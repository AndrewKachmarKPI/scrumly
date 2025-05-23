package com.scrumly.service.impl;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.domain.ActivityTimerState;
import com.scrumly.dto.backlog.IssueDto;
import com.scrumly.dto.events.ActivitySummaryNotes;
import com.scrumly.dto.events.OnUpdateMeetingNotes;
import com.scrumly.dto.events.SyncBlockOption;
import com.scrumly.dto.issues.IssueShortInfo;
import com.scrumly.dto.prompts.GenerateReflectionRQ;
import com.scrumly.dto.user.UserConnectionStatus;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.feign.IntegrationServiceFeignClient;
import com.scrumly.integration.googleGemini.GeminiApiRequest;
import com.scrumly.integration.googleGemini.GeminiApiResponse;
import com.scrumly.integration.jiraCloud.GetIssue;
import com.scrumly.service.ActivityRoomActionService;
import com.scrumly.service.ActivityRoomService;
import com.scrumly.service.backlog.BacklogIssueService;
import com.scrumly.service.prompts.GenerateGeminiPromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.scrumly.utils.GeminiPromptUtils.*;

@Service
@RequiredArgsConstructor
public class ActivityRoomActionServiceImpl implements ActivityRoomActionService {
    private final ActivityRoomService activityRoomService;
    private final BacklogIssueService backlogIssueService;
    private final IntegrationServiceFeignClient integrationServiceFeignClient;
    private final GenerateGeminiPromptService generateGeminiPromptService;

    @Override
    public void onActivityRoomNavigationChange(String username, String activityId, ActivityRoom.BlockNavigationMetadata navigationMetadata) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }

            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                return;
            }
            ActivityRoom.BlockNavigationMetadata newNavigation = activityRoom.getBlockNavigationMetadata().toBuilder()
                    .activeBlockId(navigationMetadata.getActiveBlockId())
                    .build();
            for (ActivityRoom.BlockNavigationMetadata.ActivityBlockUserNavigation userNavigation : activityRoom.getBlockNavigationMetadata().getUserNavigations()) {
                userNavigation.setActiveBlockId(navigationMetadata.getActiveBlockId());
            }
            activityRoom.setBlockNavigationMetadata(newNavigation);

            activityRoomService.saveAndNotifyAll(activityRoom);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onUserNavigationChange(String username, String activityId, String activityBlockId) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            if (activityRoom.getBlockNavigationMetadata().getUserNavigations() != null) {
                activityRoom.getBlockNavigationMetadata().getUserNavigations().stream()
                        .filter(activityBlockUserNavigation -> activityBlockUserNavigation.getUserId().equals(username))
                        .findAny()
                        .ifPresent(activityBlockUserNavigation -> {
                            // Remove quotes if present
                            String cleanedBlockId = activityBlockId.replaceAll("^\"|\"$", "");
                            activityBlockUserNavigation.setActiveBlockId(cleanedBlockId);
                        });
            }
            activityRoomService.saveAndNotifyUser(activityRoom, username);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onKickUser(String username, String activityId, String userId) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                return;
            }
            activityRoom.getJoinedUsers()
                    .removeIf(userMetadata -> userMetadata.getUserId().equals(userId));
            activityRoomService.sendUserStatusChange(activityRoom, userId, UserConnectionStatus.KICKED);
            activityRoomService.saveAndNotifyAll(activityRoom);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onAnswerQuestion(String username, String activityId, String blockId, ActivityRoom.ActivityBlock.ActivityQuestionBlock.UserQuestionAnswer userQuestionAnswer) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityQuestionBlock block) {
                if (block.getUserQuestionAnswers() == null) {
                    block.setUserQuestionAnswers(new ArrayList<>());
                }
                ActivityRoom.ActivityBlock.ActivityQuestionBlock.UserQuestionAnswer existingAnswer = block.getUserQuestionAnswers().stream()
                        .filter(questionAnswer -> questionAnswer.getUserMetadata().getUserId().equals(username))
                        .findAny().orElse(null);
                if (existingAnswer == null) {
                    block.getUserQuestionAnswers().add(userQuestionAnswer);
                } else {
                    existingAnswer.setAnswers(userQuestionAnswer.getAnswers());
                }
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onDeleteQuestionAnswer(String username, String activityId, String blockId) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityQuestionBlock block) {
                block.getUserQuestionAnswers().removeIf(answer -> answer.getUserMetadata().getUserId().equals(username));
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onCreateReflectCard(String username, String activityId, String blockId,
                                    ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard reflectCard) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityReflectBlock block) {
                ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnMetadata columnMetadata = block.getColumnMetadata().stream()
                        .filter(reflectColumnMetadata -> reflectColumnMetadata.getId().equals(reflectCard.getColumnId()))
                        .findAny().orElse(null);
                if (columnMetadata == null) {
                    throw new EntityNotFoundException("No such reflect column");
                }

                ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard reflectColumnCard = block.getColumnCards().stream()
                        .filter(columnCard -> columnCard.getColumnMetadata().getId().equals(columnMetadata.getId()))
                        .findAny()
                        .orElse(null);


                if (reflectColumnCard == null) {
                    reflectCard.setCardId(UUID.randomUUID().toString());
                    reflectCard.setOrder(1);
                    block.getColumnCards()
                            .add(ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.builder()
                                         .columnMetadata(columnMetadata)
                                         .version(1L)
                                         .userColumnReflectCards(List.of(reflectCard))
                                         .build());
                } else {
                    ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard userColumnReflectCard = reflectColumnCard.getUserColumnReflectCards().stream()
                            .filter(columnReflectCard -> columnReflectCard.getColumnId().equals(columnMetadata.getId()) &&
                                    columnReflectCard.getCardId() != null && Objects.equals(columnReflectCard.getCardId(), reflectCard.getCardId()) &&
                                    columnReflectCard.getUserMetadata().getUserId().equals(username))
                            .findAny().orElse(null);
                    if (userColumnReflectCard == null) {
                        reflectCard.setCardId(UUID.randomUUID().toString());
                        reflectCard.setOrder(reflectColumnCard.getUserColumnReflectCards().size() + 1);
                        reflectColumnCard.getUserColumnReflectCards().add(reflectCard);
                    } else {
                        userColumnReflectCard.setCardContent(reflectCard.getCardContent());
                    }
                }
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onDeleteReflectCard(String username, String activityId, String blockId,
                                    ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard reflectCard) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityReflectBlock block) {
                ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnMetadata columnMetadata = block.getColumnMetadata().stream()
                        .filter(reflectColumnMetadata -> reflectColumnMetadata.getId().equals(reflectCard.getColumnId()))
                        .findAny().orElse(null);
                if (columnMetadata == null) {
                    throw new EntityNotFoundException("No such reflect column");
                }

                ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard reflectColumnCard = block.getColumnCards().stream()
                        .filter(columnCard -> columnCard.getColumnMetadata().getId().equals(columnMetadata.getId()))
                        .findAny()
                        .orElse(null);

                if (reflectColumnCard == null) {
                    throw new EntityNotFoundException("No reflection found");
                }

                reflectColumnCard.getUserColumnReflectCards()
                        .removeIf(columnReflectCard -> columnReflectCard.getColumnId().equals(columnMetadata.getId()) &&
                                columnReflectCard.getCardId().equals(reflectCard.getCardId()) &&
                                columnReflectCard.getUserMetadata().getUserId().equals(username));
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onChangeReflectCard(String username, String activityId, String blockId,
                                    List<ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard> reflectColumnCards) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityReflectBlock block) {
                List<ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard> savedCards = block.getColumnCards();
                List<ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard> newCards = reflectColumnCards.stream()
                        .filter(reflectColumnCard -> savedCards.stream()
                                .noneMatch(column -> column.getColumnMetadata().getId().equals(reflectColumnCard.getColumnMetadata().getId())))
                        .toList();

                for (ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard newCard : newCards) {
                    block.getColumnCards().add(ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.builder()
                                                       .columnMetadata(newCard.getColumnMetadata())
                                                       .userColumnReflectCards(new ArrayList<>())
                                                       .version(1L)
                                                       .build());
                }


                for (ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard savedCard : savedCards) {
                    List<ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard> savedUserCards = savedCard.getUserColumnReflectCards();
                    List<String> savedUserCardsIds = savedUserCards.stream()
                            .map(ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard::getCardId)
                            .toList();

                    ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard columnCard = reflectColumnCards.stream()
                            .filter(reflectColumnCard -> reflectColumnCard.getColumnMetadata().getId().equals(savedCard.getColumnMetadata().getId()))
                            .findAny()
                            .orElse(null);
                    if (columnCard == null || !savedCard.getVersion().equals(columnCard.getVersion())) {
                        continue;
                    }
                    List<ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard> userCards = columnCard.getUserColumnReflectCards();
                    List<String> userCardsIds = userCards.stream()
                            .map(ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard::getCardId)
                            .toList();

                    List<String> removeIds = savedUserCardsIds.stream()
                            .filter(id -> !userCardsIds.contains(id))
                            .toList();
                    List<String> addIds = userCardsIds.stream()
                            .filter(id -> !savedUserCardsIds.contains(id))
                            .toList();
                    List<String> updateIds = savedUserCardsIds.stream()
                            .filter(userCardsIds::contains)
                            .toList();


                    savedUserCards.removeIf(columnReflectCard -> removeIds.contains(columnReflectCard.getCardId()));
                    savedUserCards.addAll(userCards.stream()
                                                  .filter(columnReflectCard -> addIds.contains(columnReflectCard.getCardId()))
                                                  .toList());
                    savedUserCards.stream()
                            .filter(columnReflectCard -> updateIds.contains(columnReflectCard.getCardId()))
                            .forEach(columnReflectCard -> userCards.stream()
                                    .filter(card -> card.getCardId().equals(columnReflectCard.getCardId()))
                                    .findAny()
                                    .ifPresent(userColumnReflectCard -> columnReflectCard.setOrder(userColumnReflectCard.getOrder())));

                    savedUserCards.forEach(columnReflectCard -> columnReflectCard.setColumnId(savedCard.getColumnMetadata().getId()));
                    savedCard.setVersion(savedCard.getVersion() + 1);

                }
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onClearReflectColumns(String username, String activityId, String blockId, List<Long> columnIds) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityReflectBlock block) {
                List<ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard> clearColumns = block.getColumnCards().stream()
                        .filter(reflectColumnCard -> columnIds.contains(reflectColumnCard.getColumnMetadata().getId()))
                        .toList();
                for (ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard clearColumn : clearColumns) {
                    clearColumn.setVersion(1L);
                    clearColumn.setUserColumnReflectCards(new ArrayList<>());
                }
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onChangeTimerState(String username, String activityId, ActivityTimerState timerState) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                return;
            }
            activityRoomService.notifyTimerChange(activityRoom, timerState);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onSelectEstimateIssues(String username, String activityId, String blockId, List<IssueShortInfo> issues) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                return;
            }

            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityEstimateBlock block) {
                String connectionId = activityRoom.getTeamMetadata().getOrganizationId();
                List<ActivityRoom.ActivityBlock.IssueMetadata> issueMetadataList = loadIssueMetadata(issues, connectionId);

                List<ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue> savedIssues = block.getEstimateIssues();
                if (savedIssues == null) {
                    savedIssues = new ArrayList<>();
                }
                List<String> savedIssuesKeys = savedIssues.stream()
                        .map(estimateIssue -> estimateIssue.getIssueMetadata().getIssueKey())
                        .toList();
                List<ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue> estimateIssues = issueMetadataList.stream()
                        .filter(issueMetadata -> !savedIssuesKeys.contains(issueMetadata.getIssueKey()))
                        .map(issueMetadata -> ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue.builder()
                                .id(UUID.randomUUID().toString())
                                .issueMetadata(issueMetadata)
                                .finalEstimate(null)
                                .isRevealed(false)
                                .userEstimateMetadata(new ArrayList<>())
                                .build())
                        .toList();
                savedIssues.addAll(estimateIssues);
                block.setEstimateIssues(savedIssues);
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onDeleteEstimateIssues(String username, String activityId, String blockId, List<String> issueIds) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                return;
            }

            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityEstimateBlock block) {
                block.getEstimateIssues().removeIf(estimateIssue -> issueIds.contains(estimateIssue.getId()));
                ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue estimateIssue = block.getEstimateIssues().stream().findFirst()
                        .orElse(null);
                block.setActiveEstimateIssueId(estimateIssue != null ? estimateIssue.getId() : null);
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onUpdateEstimateIssues(String username, String activityId, String blockId, List<String> issueIds) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                return;
            }

            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityEstimateBlock block) {
                List<ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue> estimateIssues = block.getEstimateIssues().stream()
                        .filter(estimateIssue -> issueIds.contains(estimateIssue.getId()))
                        .toList();

                // UPDATE JIRA ISSUES
                List<ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue> jiraIssues = estimateIssues.stream()
                        .filter(estimateIssue -> ServiceType.JIRA_CLOUD.toString().equals(estimateIssue.getIssueMetadata().getProvider()))
                        .toList();
                List<String> jiraKeys = jiraIssues.stream()
                        .map(estimateIssue -> estimateIssue.getIssueMetadata().getIssueKey())
                        .toList();
                if (!jiraKeys.isEmpty()) {
                    List<GetIssue> getIssues = integrationServiceFeignClient.getIssues(activityRoom.getTeamMetadata().getOrganizationId(), jiraKeys);
                    for (ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue jiraIssue : jiraIssues) {
                        GetIssue issue = getIssues.stream()
                                .filter(getIssue -> getIssue.getId().equals(jiraIssue.getIssueMetadata().getIssueId()))
                                .findAny().orElse(null);
                        if (issue == null) {
                            continue;
                        }
                        ActivityRoom.ActivityBlock.IssueMetadata newMetadata = jiraIssue.getIssueMetadata().toBuilder()
                                .description(issue.getRenderedFields().getDescription())
                                .title(issue.getFields().getSummary())
                                .build();
                        jiraIssue.setIssueMetadata(newMetadata);
                    }
                }


                List<ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue> scrumlyIssues = estimateIssues.stream()
                        .filter(estimateIssue -> ServiceType.SCRUMLY.toString().equals(estimateIssue.getIssueMetadata().getProvider()))
                        .toList();
                List<String> scrumlyKeys = scrumlyIssues.stream()
                        .map(estimateIssue -> estimateIssue.getIssueMetadata().getIssueKey())
                        .toList();
                if (!scrumlyKeys.isEmpty()) {
                    List<IssueDto> issueDtos = backlogIssueService.findIssues(scrumlyKeys);
                    for (ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue scrumlyIssue : scrumlyIssues) {
                        IssueDto issue = issueDtos.stream()
                                .filter(getIssue -> getIssue.getIssueKey().equals(scrumlyIssue.getIssueMetadata().getIssueKey()))
                                .findAny().orElse(null);
                        if (issue == null) {
                            continue;
                        }
                        ActivityRoom.ActivityBlock.IssueMetadata newMetadata = scrumlyIssue.getIssueMetadata().toBuilder()
                                .description(issue.getDescription())
                                .title(issue.getTitle())
                                .build();
                        scrumlyIssue.setIssueMetadata(newMetadata);
                    }
                }

                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }


    @Override
    public void onSelectBoardBacklogIssues(String username, String activityId, String blockId, List<IssueShortInfo> issues) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);


            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock block) {
                List<ActivityRoom.ActivityBlock.IssueMetadata> issueBacklog = block.getIssueBacklog() != null
                        ? block.getIssueBacklog()
                        : new ArrayList<>();
                List<String> selectedKeys = issueBacklog.stream()
                        .map(ActivityRoom.ActivityBlock.IssueMetadata::getIssueKey)
                        .toList();

                List<IssueShortInfo> newIssues = issues.stream()
                        .filter(issueShortInfo -> !selectedKeys.contains(issueShortInfo.getIssueKey()))
                        .toList();
                List<ActivityRoom.ActivityBlock.IssueMetadata> newIssuesMetadata = newIssues.stream()
                        .map(issueShortInfo -> ActivityRoom.ActivityBlock.IssueMetadata.builder()
                                .imgPath(issueShortInfo.getImgPath())
                                .issueUrl(issueShortInfo.getIssueUrl())
                                .issueId(issueShortInfo.getIssueId())
                                .issueKey(issueShortInfo.getIssueKey())
                                .title(issueShortInfo.getTitle())
                                .provider(issueShortInfo.getProvider().toString())
                                .estimate(issueShortInfo.getEstimate())
                                .projectName(issueShortInfo.getProjectName())
                                .projectId(issueShortInfo.getProjectId())
                                .build())
                        .toList();

                issueBacklog.addAll(newIssuesMetadata);
                block.setIssueBacklog(issueBacklog);

                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onDeleteBoardBacklogIssues(String username, String activityId, String blockId, List<String> issueIds) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock block) {
                List<ActivityRoom.ActivityBlock.IssueMetadata> issueBacklog = block.getIssueBacklog();
                issueBacklog.removeIf(issueMetadata -> issueIds.contains(issueMetadata.getIssueId()));

                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onUpdateBoardBacklogIssues(String username, String activityId, String blockId, List<String> issueIds) {

    }


    private List<ActivityRoom.ActivityBlock.IssueMetadata> loadIssueMetadata(List<IssueShortInfo> issues, String connectionId) {
        List<ActivityRoom.ActivityBlock.IssueMetadata> issueMetadataList = new ArrayList<>();

        //TODO ADD MORE PROVIDERS
        List<String> jiraKeys = issues.stream()
                .filter(issueShortInfo -> ServiceType.JIRA_CLOUD.equals(issueShortInfo.getProvider()))
                .map(IssueShortInfo::getIssueKey)
                .toList();
        if (!jiraKeys.isEmpty()) {
            List<GetIssue> getIssues = integrationServiceFeignClient.getIssues(connectionId, jiraKeys);
            for (GetIssue getIssue : getIssues) {
                IssueShortInfo shortInfo = issues.stream()
                        .filter(issueShortInfo -> issueShortInfo.getIssueKey().equals(getIssue.getKey()))
                        .findAny().orElse(null);
                if (shortInfo == null) {
                    continue;
                }

                ActivityRoom.ActivityBlock.IssueMetadata issueMetadata = ActivityRoom.ActivityBlock.IssueMetadata.builder()
                        .imgPath(shortInfo.getImgPath())
                        .issueUrl(shortInfo.getIssueUrl())
                        .issueId(getIssue.getId())
                        .issueKey(getIssue.getKey())
                        .description(getIssue.getRenderedFields().getDescription())
                        .title(getIssue.getFields().getSummary())
                        .provider(ServiceType.JIRA_CLOUD.toString())
                        .build();
                issueMetadataList.add(issueMetadata);
            }
        }

        List<String> scrumlyKeys = issues.stream()
                .filter(issueShortInfo -> ServiceType.SCRUMLY.equals(issueShortInfo.getProvider()))
                .map(IssueShortInfo::getIssueKey)
                .toList();
        if (!scrumlyKeys.isEmpty()) {
            List<IssueDto> issueDtos = backlogIssueService.findIssues(scrumlyKeys);
            for (IssueDto issueDto : issueDtos) {
                IssueShortInfo shortInfo = issues.stream()
                        .filter(issueShortInfo -> issueShortInfo.getIssueKey().equals(issueDto.getIssueKey()))
                        .findAny().orElse(null);
                if (shortInfo == null) {
                    continue;
                }
                ActivityRoom.ActivityBlock.IssueMetadata issueMetadata = ActivityRoom.ActivityBlock.IssueMetadata.builder()
                        .imgPath(shortInfo.getImgPath())
                        .issueUrl(shortInfo.getIssueUrl())
                        .issueId(issueDto.getId().toString())
                        .issueKey(issueDto.getIssueKey())
                        .description(issueDto.getDescription())
                        .title(issueDto.getTitle())
                        .provider(ServiceType.SCRUMLY.toString())
                        .projectName(issueDto.getBacklogName())
                        .build();
                issueMetadataList.add(issueMetadata);
            }
        }

        return issueMetadataList;
    }


    @Override
    public void onChangeActiveEstimateIssue(String username, String activityId, String blockId, String issueId) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                return;
            }

            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityEstimateBlock block) {
                ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue issue = block.getEstimateIssues().stream()
                        .filter(estimateIssue -> issueId.equals(estimateIssue.getId()))
                        .findAny()
                        .orElse(null);
                if (issue == null) {
                    throw new EntityNotFoundException("Issue with such id is not found");
                }
                block.setActiveEstimateIssueId(issueId);
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onOpenSearchEstimateIssue(String username, String activityId, String blockId) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                return;
            }

            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityEstimateBlock block) {
                block.setActiveEstimateIssueId(null);
                activityRoomService.saveAndNotifyUser(activityRoom, username);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onSelectEstimate(String username, String activityId, String blockId, String issueId,
                                 ActivityRoom.ActivityBlock.ActivityEstimateBlock.UserEstimateMetadata userEstimateMetadata) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);

            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityEstimateBlock block) {
                ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue issue = block.getEstimateIssues().stream()
                        .filter(estimateIssue -> issueId.equals(estimateIssue.getId()))
                        .findAny()
                        .orElse(null);
                if (issue == null) {
                    throw new EntityNotFoundException("Issue with such id is not found");
                }

                if (issue.getIsRevealed()) {
                    return;
                }

                List<ActivityRoom.ActivityBlock.ActivityEstimateBlock.UserEstimateMetadata> estimates = issue.getUserEstimateMetadata();
                if (estimates == null) {
                    issue.setUserEstimateMetadata(new ArrayList<>());
                }
                ActivityRoom.ActivityBlock.ActivityEstimateBlock.UserEstimateMetadata estimateMetadata = estimates.stream()
                        .filter(estimate -> estimate.getUserMetadata().getUserId().equals(username))
                        .findAny().orElse(null);
                if (estimateMetadata != null) {

                    if (estimateMetadata.getEstimate().equals(userEstimateMetadata.getEstimate())) {
                        estimates.removeIf(estimate -> estimate.getUserMetadata().getUserId().equals(username));
                    } else {
                        estimateMetadata.setEstimate(userEstimateMetadata.getEstimate());
                    }
                } else {
                    issue.getUserEstimateMetadata().add(userEstimateMetadata);
                }
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onRevealEstimates(String username, String activityId, String blockId, String issueId) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);

            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityEstimateBlock block) {
                ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue issue = block.getEstimateIssues().stream()
                        .filter(estimateIssue -> issueId.equals(estimateIssue.getId()))
                        .findAny()
                        .orElse(null);
                if (issue == null) {
                    throw new EntityNotFoundException("Issue with such id is not found");
                }
                issue.setIsRevealed(true);
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onHideEstimates(String username, String activityId, String blockId, String issueId) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);

            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityEstimateBlock block) {
                ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue issue = block.getEstimateIssues().stream()
                        .filter(estimateIssue -> issueId.equals(estimateIssue.getId()))
                        .findAny()
                        .orElse(null);
                if (issue == null) {
                    throw new EntityNotFoundException("Issue with such id is not found");
                }
                issue.setIsRevealed(false);
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onSelectFinalEstimate(String username, String activityId, String blockId, String issueId, String estimate) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);

            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityEstimateBlock block) {
                ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateIssue issue = block.getEstimateIssues().stream()
                        .filter(estimateIssue -> issueId.equals(estimateIssue.getId()))
                        .findAny()
                        .orElse(null);
                if (issue == null) {
                    throw new EntityNotFoundException("Issue with such id is not found");
                }
                issue.setFinalEstimate(estimate);
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onUpdateMeetingNotes(String username, String activityId, OnUpdateMeetingNotes meetingNotes) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                return;
            }
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }

            if (activityRoom.getNotesMetadata() != null) {
                activityRoom.getNotesMetadata().setNotes(meetingNotes.getNotes());
            } else {
                activityRoom.setNotesMetadata(ActivityRoom.ActivityRoomNotesMetadata.builder()
                                                      .notes(meetingNotes.getNotes())
                                                      .build());
            }
            activityRoomService.save(activityRoom);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onCreateBoardBacklogCard(String username, String activityId, String blockId,
                                         ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue boardIssueCard) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock block) {
                ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnMetadata columnMetadata = block.getColumnMetadata().stream()
                        .filter(metadata -> metadata.getId().equals(boardIssueCard.getColumnId()))
                        .findAny().orElse(null);
                if (columnMetadata == null) {
                    throw new EntityNotFoundException("No such board column");
                }

                ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues columnIssues = block.getColumnIssues().stream()
                        .filter(columnCard -> columnCard.getColumnMetadata().getId().equals(columnMetadata.getId()))
                        .findAny()
                        .orElse(null);

//                ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue boardIssue = block.getColumnIssues().stream()
//                        .flatMap(itemBoardColumnIssues -> itemBoardColumnIssues.getUserColumnIssues().stream())
//                        .filter(userIssue -> userIssue.getIssueMetadata().getIssueKey().equals(boardIssueCard.getIssueMetadata().getIssueKey()))
//                        .findAny()
//                        .orElse(null);

//                if (boardIssue != null) {
//                    return;
//                }

                ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemStatusMetadata statusMetadata = columnMetadata.getColumnIssueStatuses().stream()
                        .filter(itemStatusMetadata -> itemStatusMetadata.getBacklogId().equals(boardIssueCard.getIssueMetadata().getProjectId()))
                        .findAny()
                        .orElse(null);
                boardIssueCard.setStatusMetadata(statusMetadata);

                if (columnIssues == null) {
                    boardIssueCard.setColumnIssueId(UUID.randomUUID().toString());
                    boardIssueCard.setOrder(1);
                    block.getColumnIssues()
                            .add(ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.builder()
                                         .columnMetadata(columnMetadata)
                                         .version(1L)
                                         .userColumnIssues(List.of(boardIssueCard))
                                         .build());
                } else {
                    ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue userColumnReflectCard = columnIssues.getUserColumnIssues().stream()
                            .filter(columnReflectCard -> columnReflectCard.getColumnId().equals(columnMetadata.getId()) &&
                                    columnReflectCard.getColumnIssueId() != null && Objects.equals(columnReflectCard.getColumnIssueId(), boardIssueCard.getColumnIssueId()) &&
                                    columnReflectCard.getUserMetadata().getUserId().equals(username))
                            .findAny().orElse(null);
                    if (userColumnReflectCard == null) {
                        boardIssueCard.setColumnIssueId(UUID.randomUUID().toString());
                        boardIssueCard.setOrder(columnIssues.getUserColumnIssues().size() + 1);
                        columnIssues.getUserColumnIssues().add(boardIssueCard);
                    }
                }
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
            onRecalculateTeamLoad(username, activityId, blockId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onDeleteBoardBacklogCard(String username, String activityId, String blockId,
                                         ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue boardIssueCard) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock block) {
                ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnMetadata columnMetadata = block.getColumnMetadata().stream()
                        .filter(reflectColumnMetadata -> reflectColumnMetadata.getId().equals(boardIssueCard.getColumnId()))
                        .findAny().orElse(null);
                if (columnMetadata == null) {
                    throw new EntityNotFoundException("No such reflect column");
                }

                ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues reflectColumnCard = block.getColumnIssues().stream()
                        .filter(columnCard -> columnCard.getColumnMetadata().getId().equals(columnMetadata.getId()))
                        .findAny()
                        .orElse(null);

                if (reflectColumnCard == null) {
                    throw new EntityNotFoundException("No reflection found");
                }

                reflectColumnCard.getUserColumnIssues()
                        .removeIf(columnReflectCard -> columnReflectCard.getColumnId().equals(columnMetadata.getId()) &&
                                columnReflectCard.getColumnIssueId().equals(boardIssueCard.getColumnIssueId()) &&
                                (columnReflectCard.getUserMetadata().getUserId().equals(username) ||
                                        activityRoom.getFacilitator().getUserId().equals(username)));
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
            onRecalculateTeamLoad(username, activityId, blockId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onChangeBoardBacklogCard(String username, String activityId, String blockId,
                                         List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues> boardColumnIssues) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock block) {
                List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues> savedCards = block.getColumnIssues();
                List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues> newCards = boardColumnIssues.stream()
                        .filter(reflectColumnCard -> savedCards.stream()
                                .noneMatch(column -> column.getColumnMetadata().getId().equals(reflectColumnCard.getColumnMetadata().getId())))
                        .toList();


                for (ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues newCard : newCards) {
                    block.getColumnIssues().add(ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.builder()
                                                        .columnMetadata(newCard.getColumnMetadata())
                                                        .userColumnIssues(new ArrayList<>())
                                                        .version(1L)
                                                        .build());
                }


                for (ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues savedCard : savedCards) {
                    List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue> savedUserCards = savedCard.getUserColumnIssues();
                    List<String> savedUserCardsIds = savedUserCards.stream()
                            .map(ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue::getColumnIssueId)
                            .toList();

                    ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues columnCard = boardColumnIssues.stream()
                            .filter(reflectColumnCard -> reflectColumnCard.getColumnMetadata().getId().equals(savedCard.getColumnMetadata().getId()))
                            .findAny()
                            .orElse(null);
                    if (columnCard == null || !savedCard.getVersion().equals(columnCard.getVersion())) {
                        continue;
                    }
                    List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue> userCards = columnCard.getUserColumnIssues();
                    List<String> userCardsIds = userCards.stream()
                            .map(ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue::getColumnIssueId)
                            .toList();

                    List<String> removeIds = savedUserCardsIds.stream()
                            .filter(id -> !userCardsIds.contains(id))
                            .toList();
                    List<String> addIds = userCardsIds.stream()
                            .filter(id -> !savedUserCardsIds.contains(id))
                            .toList();
                    List<String> updateIds = savedUserCardsIds.stream()
                            .filter(userCardsIds::contains)
                            .toList();


                    savedUserCards.removeIf(columnReflectCard -> removeIds.contains(columnReflectCard.getColumnIssueId()));
                    savedUserCards.addAll(userCards.stream()
                                                  .filter(columnReflectCard -> addIds.contains(columnReflectCard.getColumnIssueId()))
                                                  .toList());
                    savedUserCards.stream()
                            .filter(columnReflectCard -> updateIds.contains(columnReflectCard.getColumnIssueId()))
                            .forEach(columnReflectCard -> userCards.stream()
                                    .filter(card -> card.getColumnIssueId().equals(columnReflectCard.getColumnIssueId()))
                                    .findAny()
                                    .ifPresent(userColumnReflectCard -> columnReflectCard.setOrder(userColumnReflectCard.getOrder())));

                    savedUserCards.forEach(columnReflectCard -> columnReflectCard.setColumnId(savedCard.getColumnMetadata().getId()));
                    savedCard.setVersion(savedCard.getVersion() + 1);
                }

                updateUserBoardColumnStatus(block);
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
            onRecalculateTeamLoad(username, activityId, blockId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    private static void updateUserBoardColumnStatus(ActivityRoom.ActivityBlock.ActivityItemBoardBlock block) {
        for (ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues columnIssue : block.getColumnIssues()) {
            for (ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue userColumnIssue : columnIssue.getUserColumnIssues()) {
                ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemStatusMetadata statusMetadata = columnIssue.getColumnMetadata().getColumnIssueStatuses().stream()
                        .filter(itemStatusMetadata -> itemStatusMetadata.getBacklogId().equals(userColumnIssue.getIssueMetadata().getProjectId()))
                        .findAny().orElse(null);
                userColumnIssue.setStatusMetadata(statusMetadata);
            }
        }
    }

    @Override
    public void onClearBoardBacklogColumns(String username, String activityId, String blockId, List<Long> columnIds) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock block) {
                List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues> clearColumns = block.getColumnIssues().stream()
                        .filter(reflectColumnCard -> columnIds.contains(reflectColumnCard.getColumnMetadata().getId()))
                        .toList();
                for (ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues clearColumn : clearColumns) {
                    clearColumn.setVersion(1L);
                    clearColumn.setUserColumnIssues(new ArrayList<>());
                }
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
            onRecalculateTeamLoad(username, activityId, blockId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onSelectItemBoardColumnIssueAssignee(String username, String activityId, String blockId, ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue userIssue) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock block) {
                ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue updatedIssue = block.getColumnIssues().stream()
                        .flatMap(itemBoardColumnIssues -> itemBoardColumnIssues.getUserColumnIssues().stream())
                        .filter(userIssue1 -> userIssue1.getIssueMetadata().getIssueKey().equals(userIssue.getIssueMetadata().getIssueKey()))
                        .findAny()
                        .orElse(null);
                if (updatedIssue != null) {
                    updatedIssue.setUserMetadata(userIssue.getUserMetadata());
                    activityRoomService.saveAndNotifyAll(activityRoom);
                }
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }

        onRecalculateTeamLoad(username, activityId, blockId);
    }

    @Override
    public void onRecalculateTeamLoad(String username, String activityId, String blockId) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock block) {

                ActivityRoom.ActivityBlock.ActivityItemBoardBlock.TeamLoadMetadata teamLoad = block.getTeamLoadMetadata();
                if (teamLoad == null) {
                    teamLoad = new ActivityRoom.ActivityBlock.ActivityItemBoardBlock.TeamLoadMetadata();
                }

                List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue> userColumnIssues = block.getColumnIssues().stream()
                        .flatMap(itemBoardColumnIssues -> itemBoardColumnIssues.getUserColumnIssues().stream())
                        .toList();

                Integer totalItems = userColumnIssues.size();
                Double totalEstimation = getTotalEstimationSum(userColumnIssues);
                teamLoad.setTotalItems(totalItems);
                teamLoad.setTotalEstimation(totalEstimation);

                List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemStatusMetadata> doneStatuses = teamLoad.getDoneStatuses();
                List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemStatusMetadata> inProgressStatuses = teamLoad.getInProgressStatuses();

                List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.TeamLoadMetadata.TeamMemberLoadMetadata> membersLoadMetadata = new ArrayList<>();

                for (ActivityRoom.UserMetadata teamMember : block.getTeamMembers()) {
                    List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue> memberIssues = userColumnIssues.stream()
                            .filter(userIssue -> userIssue.getUserMetadata().getUserId().equals(teamMember.getUserId()))
                            .toList();

                    List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue> memberTodo = memberIssues.stream()
                            .filter(userIssue -> doneStatuses.stream()
                                    .noneMatch(statusMetadata -> userIssue.getStatusMetadata().getStatusId().equals(statusMetadata.getStatusId()) &&
                                            userIssue.getStatusMetadata().getBacklogId().equals(statusMetadata.getBacklogId())) &&
                                    inProgressStatuses.stream()
                                            .noneMatch(statusMetadata -> userIssue.getStatusMetadata().getStatusId().equals(statusMetadata.getStatusId()) &&
                                                    userIssue.getStatusMetadata().getBacklogId().equals(statusMetadata.getBacklogId())))
                            .toList();
                    List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue> memberDoneIssues = memberIssues.stream()
                            .filter(userIssue -> doneStatuses.stream()
                                    .anyMatch(statusMetadata -> userIssue.getStatusMetadata().getStatusId().equals(statusMetadata.getStatusId()) &&
                                            userIssue.getStatusMetadata().getBacklogId().equals(statusMetadata.getBacklogId())))
                            .toList();
                    List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue> memberProgressIssues = memberIssues.stream()
                            .filter(userIssue -> inProgressStatuses.stream()
                                    .anyMatch(statusMetadata -> userIssue.getStatusMetadata().getStatusId().equals(statusMetadata.getStatusId()) &&
                                            userIssue.getStatusMetadata().getBacklogId().equals(statusMetadata.getBacklogId())))
                            .toList();

                    Double committedLoad = getTotalEstimationSum(memberTodo);
                    Double doneLoad = getTotalEstimationSum(memberDoneIssues);
                    Double loadInProgress = getTotalEstimationSum(memberProgressIssues);
                    Double totalCapacity = committedLoad + doneLoad + loadInProgress;
                    Double progress = (committedLoad > 0) ? (doneLoad / committedLoad) * 100 : 0.0;

                    double donePercentage = (totalCapacity > 0) ? (doneLoad / totalCapacity) * 100 : 0.0;
                    double inProgressPercentage = (totalCapacity > 0) ? (loadInProgress / totalCapacity) * 100 : 0.0;
                    double todoPercentage = (totalCapacity > 0) ? (committedLoad / totalCapacity) * 100 : 0.0;
                    ActivityRoom.ActivityBlock.ActivityItemBoardBlock.TeamLoadMetadata.TeamMemberLoadMetadata teamMemberLoadMetadata = ActivityRoom.ActivityBlock.ActivityItemBoardBlock.TeamLoadMetadata.TeamMemberLoadMetadata.builder()
                            .totalItems(memberIssues.size())
                            .totalCapacity(totalCapacity)
                            .committedLoad(committedLoad)
                            .doneLoad(doneLoad)
                            .loadInProgress(loadInProgress)
                            .progress(roundDouble(progress))
                            .meterProgressMetadata(ActivityRoom.ActivityBlock.ActivityItemBoardBlock.TeamLoadMetadata.MeterProgressMetadata.builder()
                                                           .donePercentage(roundDouble(donePercentage))
                                                           .inProgressPercentage(roundDouble(inProgressPercentage))
                                                           .todoPercentage(roundDouble(todoPercentage))
                                                           .build())
                            .userMetadata(teamMember)
                            .build();
                    membersLoadMetadata.add(teamMemberLoadMetadata);
                }

                teamLoad.setMembersLoadMetadata(membersLoadMetadata);
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    private static Double roundDouble(Double progress) {
        return Math.round(progress * 100.0) / 100.0;
    }

    private static double getTotalEstimationSum(List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue> userColumnIssues) {
        return userColumnIssues.stream()
                .map(userIssue -> userIssue.getIssueMetadata().getEstimate())
                .filter(Objects::nonNull)
                .map(estimate -> {
                    try {
                        return Double.parseDouble(estimate);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    @Override
    public void onClearBacklog(String username, String activityId, String blockId) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock block) {
                block.setIssueBacklog(new ArrayList<>());
                activityRoomService.saveAndNotifyAll(activityRoom);
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }

    }

    @Override
    public void onChangeIssueEstimation(String username, String activityId, String blockId,
                                        ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue userIssue) {
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock block) {
                ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue updatedIssue = block.getColumnIssues().stream()
                        .flatMap(itemBoardColumnIssues -> itemBoardColumnIssues.getUserColumnIssues().stream())
                        .filter(userIssue1 -> userIssue1.getIssueMetadata().getIssueKey().equals(userIssue.getIssueMetadata().getIssueKey()))
                        .findAny()
                        .orElse(null);
                if (updatedIssue != null) {
                    updatedIssue.getIssueMetadata().setEstimate(userIssue.getIssueMetadata().getEstimate());
                    activityRoomService.saveAndNotifyAll(activityRoom);
                }
            } else {
                throw new EntityNotFoundException("Invalid block type");
            }
            onRecalculateTeamLoad(username, activityId, blockId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public void onSyncActivityBlock(String username, String activityId, String blockId, SyncBlockOption syncBlockOption) {
        try {
            ActivityRoom syncActivityRoom = activityRoomService.getActivityRoomOrThrow(syncBlockOption.getActivityId());
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                return;
            }
            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                return;
            }

            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            ActivityRoom.ActivityBlock syncActivityBlock = syncActivityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (syncActivityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }


            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityQuestionBlock block &&
                    syncActivityBlock instanceof ActivityRoom.ActivityBlock.ActivityQuestionBlock syncBlock) {
                int index = activityRoom.getActivityBlocks().indexOf(block);
                block = new ActivityRoom.ActivityBlock.ActivityQuestionBlock(syncBlock);
                activityRoom.getActivityBlocks().set(index, block);
            } else if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityReflectBlock block &&
                    syncActivityBlock instanceof ActivityRoom.ActivityBlock.ActivityReflectBlock syncBlock) {
                int index = activityRoom.getActivityBlocks().indexOf(block);
                block = new ActivityRoom.ActivityBlock.ActivityReflectBlock(syncBlock);
                activityRoom.getActivityBlocks().set(index, block);
            } else if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock block &&
                    syncActivityBlock instanceof ActivityRoom.ActivityBlock.ActivityItemBoardBlock syncBlock) {
                int index = activityRoom.getActivityBlocks().indexOf(block);
                block = new ActivityRoom.ActivityBlock.ActivityItemBoardBlock(syncBlock);
                activityRoom.getActivityBlocks().set(index, block);
            } else if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityEstimateBlock block &&
                    syncActivityBlock instanceof ActivityRoom.ActivityBlock.ActivityEstimateBlock syncBlock) {
                int index = activityRoom.getActivityBlocks().indexOf(block);
                block = new ActivityRoom.ActivityBlock.ActivityEstimateBlock(syncBlock);
                activityRoom.getActivityBlocks().set(index, block);
            }

            activityRoomService.saveAndNotifyAll(activityRoom);
            onRecalculateTeamLoad(username, activityId, blockId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Override
    public ActivitySummaryNotes onGenerateActivitySummary(String username, String activityId) {
        ActivitySummaryNotes summaryNotes = null;
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                throw new ServiceErrorException("You are not a facilitator");
            }

            GeminiApiRequest geminiApiRequest = generateGeminiPromptService.getRequest(activityRoom, 1.0);
            GeminiApiResponse geminiApiResponse = integrationServiceFeignClient.generateContent(geminiApiRequest);
            if (geminiApiResponse == null) {
                throw new ServiceErrorException("Failed to generate event summary");
            }

            String flatResponse = getFlatResponse(geminiApiResponse);
            System.out.println("=================RESPONSE===================");
            System.out.println(flatResponse);
            System.out.println("=================RESPONSE===================");

            summaryNotes = ActivitySummaryNotes.builder()
                    .summary(flatResponse)
                    .activityId(activityId)
                    .build();

            activityRoom.setNotesMetadata(ActivityRoom.ActivityRoomNotesMetadata.builder()
                                                  .notes(summaryNotes.getSummary())
                                                  .build());
            activityRoomService.saveAndNotifyUser(activityRoom, username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return summaryNotes;
    }

    @Override
    public ActivityRoom.ActivityRoomNotesMetadata onSetMeetingNotes(String username, String activityId, String notes) {
        ActivityRoom.ActivityRoomNotesMetadata notesMetadata = null;
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getFacilitator().getUserId().equals(username)) {
                throw new ServiceErrorException("You are not a facilitator");
            }
            notesMetadata = activityRoom.getNotesMetadata();
            if (notesMetadata == null) {
                activityRoom.setNotesMetadata(new ActivityRoom.ActivityRoomNotesMetadata(notes));
            } else {
                activityRoom.getNotesMetadata().setNotes(notes);
            }
            activityRoomService.save(activityRoom);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException("Failed to generate event summary");
        }
        return notesMetadata;
    }

    @Override
    public ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard onGenerateColumnReflection(String username, String activityId, String blockId, String prompt,
                                                                                                                              ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnMetadata reflectColumnMetadata) {
        ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard reflectCard = null;
        try {
            ActivityRoom activityRoom = activityRoomService.getActivityRoomOrThrow(activityId);
            if (!activityRoom.getStatusMetadata().getIsActive()) {
                throw new ServiceErrorException("Failed to generate reflection summary");
            }
            ActivityRoom.ActivityBlock activityBlock = activityRoom.getActivityBlocks().stream()
                    .filter(block -> block.getMetadata().getId().equals(blockId))
                    .findAny()
                    .orElse(null);
            if (activityBlock == null) {
                throw new EntityNotFoundException("Block with such id is not found");
            }
            if (activityBlock instanceof ActivityRoom.ActivityBlock.ActivityReflectBlock block) {
                ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard reflectColumnCard = block.getColumnCards().stream()
                        .filter(columnCard -> columnCard.getColumnMetadata().getId().equals(reflectColumnMetadata.getId()))
                        .findAny()
                        .orElse(null);

                GenerateReflectionRQ generateReflectionRQ = GenerateReflectionRQ.builder()
                        .prompt(prompt)
                        .columnMetadata(reflectColumnMetadata)
                        .columnCard(reflectColumnCard)
                        .build();
                GeminiApiRequest geminiApiRequest = generateGeminiPromptService.getRequest(generateReflectionRQ, 2.0);
                GeminiApiResponse geminiApiResponse = integrationServiceFeignClient.generateContent(geminiApiRequest);
                if (geminiApiResponse == null) {
                    throw new ServiceErrorException("Failed to generate reflection summary");
                }
                String flatResponse = getFlatResponse(geminiApiResponse);
                reflectCard = ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard.builder()
                        .cardContent(flatResponse)
                        .build();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return reflectCard;
    }
}
