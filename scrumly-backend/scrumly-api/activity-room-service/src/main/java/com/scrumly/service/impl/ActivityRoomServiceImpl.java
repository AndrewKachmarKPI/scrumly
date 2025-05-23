package com.scrumly.service.impl;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.domain.ActivityRoomEntity;
import com.scrumly.domain.ActivityRoomReport;
import com.scrumly.domain.ActivityTimerState;
import com.scrumly.dto.events.OnActivityRoomClosed;
import com.scrumly.dto.events.SyncBlockOption;
import com.scrumly.dto.user.TeamMetadataDto;
import com.scrumly.dto.user.UserConnectionStatus;
import com.scrumly.dto.user.UserProfileDto;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.event.dto.ActivityBlockConfigDto;
import com.scrumly.event.dto.ActivityTemplateDto;
import com.scrumly.event.dto.activity.ActivityDto;
import com.scrumly.event.dto.blocks.estimate.EstimateBlockDto;
import com.scrumly.event.dto.blocks.itemsBoard.ItemsBoardBlockDto;
import com.scrumly.event.dto.blocks.question.QuestionBlockDto;
import com.scrumly.event.dto.blocks.reflect.ReflectBlockDto;
import com.scrumly.event.enums.ActivityBlockType;
import com.scrumly.event.enums.ActivityStatus;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.feign.EventServiceFeignClient;
import com.scrumly.feign.UserServiceFeignClient;
import com.scrumly.mappers.BusinessMapper;
import com.scrumly.messaging.MessageProducer;
import com.scrumly.messaging.dto.ExportIssueDto;
import com.scrumly.messaging.topic.TopicConfiguration;
import com.scrumly.repository.ActivityRoomRepository;
import com.scrumly.service.ActivityRoomReportService;
import com.scrumly.service.ActivityRoomService;
import com.scrumly.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.scrumly.mappers.BusinessMapper.*;
import static com.scrumly.utils.SecurityUtils.getUsername;


@Service
@RequiredArgsConstructor
public class ActivityRoomServiceImpl implements ActivityRoomService {
    private final EventServiceFeignClient eventServiceFeignClient;
    private final UserServiceFeignClient userServiceFeignClient;
    private final ActivityRoomRepository roomRepository;
    private final BusinessMapper businessMapper;
    private final WebSocketService webSocketService;
    private final ActivityRoomReportService roomReportService;
    private final MessageProducer messageProducer;

    @Override
    @Transactional
    public ActivityRoom createRoom(String activityId) {
        ActivityRoomEntity room = roomRepository.findActivityRoomByActivityId(activityId);
        if (room != null) {
            throw new DuplicateEntityException("Room for this activity already exists");
        }

        ActivityDto activityDto = eventServiceFeignClient.getActivityById(activityId);
        if (!activityDto.getCreatedBy().equals(getUsername())) {
            throw new AccessDeniedException("You cannot start activity room ask activity creator");
        }

        ActivityTemplateDto templateDto = activityDto.getActivityTemplate();
        if (templateDto == null) {
            throw new EntityNotFoundException("Workspace template is null");
        }

        ActivityRoom.UserMetadata facilitatorMetadata = getRoomFacilitatorMetadata();

        ActivityRoom activityRoom = ActivityRoom.builder()
                .id(UUID.randomUUID().toString())
                .activityId(activityId)
                .activityName(activityDto.getScheduledEvent() != null
                                      ? activityDto.getScheduledEvent().getTitle()
                                      : templateDto.getName())
                .statusMetadata(getRoomStatusMetadata(facilitatorMetadata))
                .joinedUsers(new ArrayList<>())
                .templateMetadata(getRoomActivityTemplateMetadata(templateDto))
                .teamMetadata(getRoomTeamMetadata(activityDto))
                .facilitator(facilitatorMetadata)
                .blockNavigationMetadata(getRoomNavigationMetadata(templateDto))
                .activityBlocks(getRoomActivityBlocks(templateDto, activityDto.getTeamId()))
                .build();

        room = ActivityRoomEntity.builder()
                .roomId(activityRoom.getId())
                .activityId(activityId)
                .roomPayload(serializeMeetingRoom(activityRoom))
                .build();
        room = roomRepository.save(room);

        try {
            eventServiceFeignClient.saveRoomReference(activityId, room.getRoomId());
            eventServiceFeignClient.changeActivityStatus(activityId, ActivityStatus.STARTED);
        } catch (Exception e) {
            e.printStackTrace();
        }

        webSocketService.sendActivityRoomCreated(activityRoom);
        return activityRoom;
    }

    private ActivityRoomEntity getActivityRoomEntity(String activityId) {
        ActivityRoomEntity room = roomRepository.findActivityRoomByActivityId(activityId);
        if (room == null) {
            throw new EntityNotFoundException("Activity room is not found");
        }
        return room;
    }

    private ActivityRoom.ActivityRoomStatusMetadata getRoomStatusMetadata(ActivityRoom.UserMetadata userMetadata) {
        return ActivityRoom.ActivityRoomStatusMetadata.builder()
                .isActive(true)
                .currentStatus(ActivityRoom.ActivityRoomStatusMetadata.ActivityRoomStatus.ACTIVE)
                .statusChangeMetadata(Arrays.asList(
                        ActivityRoom.ActivityRoomStatusMetadata.ActivityRoomStatusChangeMetadata.builder()
                                .status(ActivityRoom.ActivityRoomStatusMetadata.ActivityRoomStatus.ACTIVE)
                                .prevStatus(null)
                                .userMetadata(userMetadata)
                                .build()
                ))
                .timeMetadata(ActivityRoom.ActivityRoomStatusMetadata.TimeMetadata.builder()
                                      .startDateTime(LocalDateTime.now())
                                      .build())
                .build();
    }


    private ActivityRoom.ActivityTemplateMetadata getRoomActivityTemplateMetadata(ActivityTemplateDto templateDto) {
        return ActivityRoom.ActivityTemplateMetadata.builder()
                .activityId(templateDto.getTemplateId())
                .name(templateDto.getName())
                .type(templateDto.getType().getType())
                .build();
    }

    private ActivityRoom.TeamMetadata getRoomTeamMetadata(ActivityDto activityDto) {
        TeamMetadataDto teamMetadataDto = userServiceFeignClient.findTeamMetadata(activityDto.getTeamId());
        if (teamMetadataDto == null) {
            throw new IllegalStateException("Cannot create activity room");
        }
        return ActivityRoom.TeamMetadata.builder()
                .organizationId(teamMetadataDto.getOrganizationId())
                .teamId(teamMetadataDto.getTeamId())
                .organizationName(teamMetadataDto.getOrganizationName())
                .teamName(teamMetadataDto.getTeamName())
                .build();
    }

    private ActivityRoom.UserMetadata getRoomFacilitatorMetadata() {
        UserProfileDto userProfileDto = userServiceFeignClient.findMyUserProfile().getBody();
        if (userProfileDto == null) {
            throw new IllegalStateException("Cannot create activity room");
        }
        return businessMapper.userProfileToMetadata(userProfileDto);
    }

    private ActivityRoom.BlockNavigationMetadata getRoomNavigationMetadata(ActivityTemplateDto templateDto) {
        List<ActivityRoom.BlockNavigationMetadata.ActivityBlockMetadata> blocks = templateDto.getBlocks().stream()
                .map(businessMapper::blockConfigDtoToBlockMetadata)
                .sorted(Comparator.comparing(ActivityRoom.BlockNavigationMetadata.ActivityBlockMetadata::getOrder))
                .toList();
        if (!blocks.isEmpty()) {
            String activeBlockId = blocks.get(0).getId();
            return ActivityRoom.BlockNavigationMetadata.builder()
                    .activeBlockId(activeBlockId)
                    .blocks(blocks)
                    .userNavigations(new ArrayList<>())
                    .build();
        }
        return null;
    }


    private List<ActivityRoom.ActivityBlock> getRoomActivityBlocks(ActivityTemplateDto templateDto, String teamId) {
        List<ActivityRoom.ActivityBlock> blocks = new ArrayList<>();

        for (ActivityBlockConfigDto blockConfig : templateDto.getBlocks()) {
            ActivityRoom.BlockNavigationMetadata.ActivityBlockMetadata metadata = businessMapper.blockConfigDtoToBlockMetadata(blockConfig);

            if (blockConfig.getBlockDto() instanceof QuestionBlockDto block) {
                ActivityRoom.ActivityBlock.ActivityQuestionBlock questionBlock = ActivityRoom.ActivityBlock.ActivityQuestionBlock.builder()
                        .metadata(metadata)
                        .questionMetadata(block.getQuestions().stream()
                                                  .map(questionDto -> ActivityRoom.ActivityBlock.ActivityQuestionBlock.QuestionMetadata.builder()
                                                          .id(questionDto.getId())
                                                          .question(questionDto.getQuestion())
                                                          .answerOptions(questionDto.getAnswerOptions())
                                                          .build())
                                                  .toList())
                        .userQuestionAnswers(new ArrayList<>())
                        .build();

                blocks.add(questionBlock);
            } else if (blockConfig.getBlockDto() instanceof ReflectBlockDto block) {
                ActivityRoom.ActivityBlock.ActivityReflectBlock reflectBlock = ActivityRoom.ActivityBlock.ActivityReflectBlock.builder()
                        .metadata(metadata)
                        .columnMetadata(block.getReflectColumns().stream()
                                                .map(reflectColumnDto -> ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnMetadata.builder()
                                                        .id(reflectColumnDto.getId())
                                                        .order(reflectColumnDto.getColumnOrder())
                                                        .title(reflectColumnDto.getTitle())
                                                        .color(reflectColumnDto.getColor())
                                                        .instruction(reflectColumnDto.getInstruction())
                                                        .build())
                                                .toList())
                        .columnCards(new ArrayList<>())
                        .build();

                blocks.add(reflectBlock);
            } else if (blockConfig.getBlockDto() instanceof EstimateBlockDto block) {
                ActivityRoom.ActivityBlock.ActivityEstimateBlock estimateBlock = ActivityRoom.ActivityBlock.ActivityEstimateBlock.builder()
                        .metadata(metadata)
                        .scaleMetadata(ActivityRoom.ActivityBlock.ActivityEstimateBlock.EstimateScaleMetadata.builder()
                                               .estimateMethod(block.getEstimateMethod().toString())
                                               .name(block.getScale().getName())
                                               .scale(block.getScale().getScale())
                                               .build())
                        .estimateIssues(new ArrayList<>())
                        .build();

                blocks.add(estimateBlock);
            } else if (blockConfig.getBlockDto() instanceof ItemsBoardBlockDto block) {
                List<ActivityRoom.UserMetadata> users = userServiceFeignClient.findTeamUsers(teamId)
                        .stream().map(businessMapper::userProfileToMetadata)
                        .toList();
                ActivityRoom.ActivityBlock.ActivityItemBoardBlock itemBoardBlock = ActivityRoom.ActivityBlock.ActivityItemBoardBlock.builder()
                        .metadata(metadata)
                        .columnMetadata(block.getBoardColumns().stream()
                                                .map(reflectColumnDto -> ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnMetadata.builder()
                                                        .id(reflectColumnDto.getId())
                                                        .order(reflectColumnDto.getColumnOrder())
                                                        .title(reflectColumnDto.getTitle())
                                                        .instruction(reflectColumnDto.getInstruction())
                                                        .color(reflectColumnDto.getColor())
                                                        .columnIssueStatuses(reflectColumnDto.getStatusMapping().stream()
                                                                                     .map(itemsBoardColumnStatusDto -> ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemStatusMetadata.builder()
                                                                                             .statusId(itemsBoardColumnStatusDto.getStatusId())
                                                                                             .statusName(itemsBoardColumnStatusDto.getStatus())
                                                                                             .backlogId(itemsBoardColumnStatusDto.getBacklogId())
                                                                                             .color(itemsBoardColumnStatusDto.getColor())
                                                                                             .build())
                                                                                     .toList())
                                                        .build())
                                                .toList())
                        .columnIssues(new ArrayList<>())
                        .teamMembers(users)
                        .teamLoadMetadata(ActivityRoom.ActivityBlock.ActivityItemBoardBlock.TeamLoadMetadata.builder()
                                                  .totalEstimation(0.0)
                                                  .doneStatuses(block.getDoneStatuses().stream()
                                                                        .map(itemsBoardColumnStatusDto -> ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemStatusMetadata.builder()
                                                                                .statusId(itemsBoardColumnStatusDto.getStatusId())
                                                                                .statusName(itemsBoardColumnStatusDto.getStatus())
                                                                                .backlogId(itemsBoardColumnStatusDto.getBacklogId())
                                                                                .color(itemsBoardColumnStatusDto.getColor())
                                                                                .build())
                                                                        .toList())
                                                  .inProgressStatuses(block.getInProgressStatuses().stream()
                                                                              .map(itemsBoardColumnStatusDto -> ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemStatusMetadata.builder()
                                                                                      .statusId(itemsBoardColumnStatusDto.getStatusId())
                                                                                      .statusName(itemsBoardColumnStatusDto.getStatus())
                                                                                      .backlogId(itemsBoardColumnStatusDto.getBacklogId())
                                                                                      .color(itemsBoardColumnStatusDto.getColor())
                                                                                      .build())
                                                                              .toList())
                                                  .membersLoadMetadata(users.stream()
                                                                               .map(userMetadata -> ActivityRoom.ActivityBlock.ActivityItemBoardBlock.TeamLoadMetadata.TeamMemberLoadMetadata.builder()
                                                                                       .committedLoad(0.0)
                                                                                       .doneLoad(0.0)
                                                                                       .loadInProgress(0.0)
                                                                                       .meterProgressMetadata(ActivityRoom.ActivityBlock.ActivityItemBoardBlock.TeamLoadMetadata.MeterProgressMetadata.builder()
                                                                                                                      .todoPercentage(0.0)
                                                                                                                      .inProgressPercentage(0.0)
                                                                                                                      .donePercentage(0.0)
                                                                                                                      .build())
                                                                                       .userMetadata(userMetadata)
                                                                                       .build())
                                                                               .toList())
                                                  .build())
                        .build();

                blocks.add(itemBoardBlock);
            }
        }
        return blocks;
    }

    @Override
    public void onFinishActivity(String activityId) {
        try {
            ActivityRoomEntity room = roomRepository.findActivityRoomByActivityId(activityId);

            ActivityRoom activityRoom = getActivityRoomOrThrow(activityId);
            if (!activityRoom.getFacilitator().getUserId().equals(getUsername())) {
                throw new ServiceErrorException("Only facilitator can close this activity");
            }
            ActivityRoom.ActivityRoomStatusMetadata statusMetadata = activityRoom.getStatusMetadata();
            statusMetadata.setIsActive(false);
            statusMetadata.setCurrentStatus(ActivityRoom.ActivityRoomStatusMetadata.ActivityRoomStatus.CLOSED);
            statusMetadata.getStatusChangeMetadata()
                    .add(ActivityRoom.ActivityRoomStatusMetadata.ActivityRoomStatusChangeMetadata.builder()
                                 .userMetadata(activityRoom.getFacilitator())
                                 .prevStatus(ActivityRoom.ActivityRoomStatusMetadata.ActivityRoomStatus.ACTIVE)
                                 .status(ActivityRoom.ActivityRoomStatusMetadata.ActivityRoomStatus.CLOSED)
                                 .build());
            LocalDateTime startDate = statusMetadata.getTimeMetadata().getStartDateTime();
            LocalDateTime endDate = LocalDateTime.now();
            Long activityDuration = Duration.between(startDate, endDate).toMillis();
            statusMetadata.setTimeMetadata(statusMetadata.getTimeMetadata().toBuilder()
                                                   .finishDateTime(endDate)
                                                   .duration(activityDuration)
                                                   .build());

            room.setRoomPayload(serializeMeetingRoom(activityRoom));
            roomRepository.save(room);

            ActivityRoomReport roomReport = roomReportService.createActivityRoomReport(activityRoom);
            OnActivityRoomClosed activityRoomClosed = OnActivityRoomClosed.builder()
                    .activityRoomReport(serializeMeetingRoomReport(roomReport))
                    .activityId(activityId)
                    .activityRoom(serializeMeetingRoom(activityRoom))
                    .build();
            webSocketService.sendActivityRoomClosed(activityRoomClosed);

            try {
                eventServiceFeignClient.changeActivityStatus(activityId, ActivityStatus.FINISHED);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                exportEstimateBlockChanges(activityRoom);
                exportItemBoardChanges(activityRoom);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }


    private void exportItemBoardChanges(ActivityRoom activityRoom) {
        try {
            List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock> itemBoardBlocks = activityRoom.getActivityBlocks().stream()
                    .filter(activityBlock -> activityBlock.getMetadata().getType()
                            .equals(ActivityBlockType.ITEM_BOARD_BLOCK.toString()))
                    .map(activityBlock -> (ActivityRoom.ActivityBlock.ActivityItemBoardBlock) activityBlock)
                    .toList();

            for (ActivityRoom.ActivityBlock.ActivityItemBoardBlock itemBoardBlock : itemBoardBlocks) {
                List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue> userIssues = itemBoardBlock.getColumnIssues().stream()
                        .flatMap(itemBoardColumnIssues -> itemBoardColumnIssues.getUserColumnIssues().stream())
                        .toList();
                List<ExportIssueDto.IssueChangesDto> jiraIssues = getIssueChangesDtos(ServiceType.JIRA_CLOUD, userIssues);
                ExportIssueDto externalExportIssueDto = ExportIssueDto.builder()
                        .connectingId(activityRoom.getTeamMetadata().getOrganizationId())
                        .provider(ServiceType.JIRA_CLOUD)
                        .issues(jiraIssues)
                        .build();
                if (externalExportIssueDto != null && !externalExportIssueDto.getIssues().isEmpty()) {
                    messageProducer.sendExportIssueEvent(TopicConfiguration.QUEUE_EXPORT_JIRA_ISSUE, externalExportIssueDto);
                }

                List<ExportIssueDto.IssueChangesDto> scrumlyIssues = getIssueChangesDtos(ServiceType.SCRUMLY, userIssues);
                ExportIssueDto internalExportIssueDto = ExportIssueDto.builder()
                        .connectingId(activityRoom.getTeamMetadata().getOrganizationId())
                        .provider(ServiceType.SCRUMLY)
                        .issues(scrumlyIssues)
                        .activityName(activityRoom.getActivityName())
                        .activityId(activityRoom.getActivityId())
                        .build();
                if (internalExportIssueDto != null && !internalExportIssueDto.getIssues().isEmpty()) {
                    messageProducer.sendExportIssueEvent(TopicConfiguration.QUEUE_EXPORT_SCRUMLY_ISSUE, internalExportIssueDto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<ExportIssueDto.IssueChangesDto> getIssueChangesDtos(ServiceType serviceType,
                                                                     List<ActivityRoom.ActivityBlock.ActivityItemBoardBlock.ItemBoardColumnIssues.ItemBoardColumnUserIssue> userIssues) {
        return userIssues
                .stream().filter(issue -> issue.getIssueMetadata().getProvider().equals(serviceType.toString()))
                .map(issue -> ExportIssueDto.IssueChangesDto.builder()
                        .issueId(issue.getIssueMetadata().getIssueKey())
                        .estimation(issue.getIssueMetadata().getEstimate())
                        .status(issue.getStatusMetadata() != null ? issue.getStatusMetadata().getStatusName() : null)
                        .statusId(issue.getStatusMetadata() != null ? issue.getStatusMetadata().getStatusId() : null)
                        .backlogId(issue.getStatusMetadata() != null ? issue.getStatusMetadata().getBacklogId() : null)
                        .assigneeId(issue.getUserMetadata().getUserId())
                        .assigneeEmail(issue.getUserMetadata().getEmail())
                        .build())
                .toList();
    }

    private void exportEstimateBlockChanges(ActivityRoom activityRoom) {
        try {
            List<ActivityRoom.ActivityBlock.ActivityEstimateBlock> estimationBlocks = activityRoom.getActivityBlocks().stream()
                    .filter(activityBlock -> activityBlock.getMetadata().getType()
                            .equals(ActivityBlockType.ESTIMATE_BLOCK.toString()))
                    .map(activityBlock -> (ActivityRoom.ActivityBlock.ActivityEstimateBlock) activityBlock)
                    .toList();

            // EXPORT EXTERNAL ESTIMATIONS
            ExportIssueDto externalExportIssueDto = null;
            for (ActivityRoom.ActivityBlock.ActivityEstimateBlock estimationBlock : estimationBlocks) {
                List<ExportIssueDto.IssueChangesDto> jiraIssues = estimationBlock.getEstimateIssues()
                        .stream().filter(estimateIssue -> estimateIssue.getIssueMetadata().getProvider().equals(ServiceType.JIRA_CLOUD.toString()))
                        .map(estimateIssue -> ExportIssueDto.IssueChangesDto.builder()
                                .issueId(estimateIssue.getIssueMetadata().getIssueKey())
                                .estimation(estimateIssue.getFinalEstimate())
                                .build())
                        .toList();
                externalExportIssueDto = ExportIssueDto.builder()
                        .connectingId(activityRoom.getTeamMetadata().getOrganizationId())
                        .provider(ServiceType.JIRA_CLOUD)
                        .issues(jiraIssues)
                        .build();
            }
            if (externalExportIssueDto != null && !externalExportIssueDto.getIssues().isEmpty()) {
                messageProducer.sendExportIssueEvent(TopicConfiguration.QUEUE_EXPORT_JIRA_ISSUE, externalExportIssueDto);
            }


            ExportIssueDto internalExportIssueDto = null;
            for (ActivityRoom.ActivityBlock.ActivityEstimateBlock estimationBlock : estimationBlocks) {
                List<ExportIssueDto.IssueChangesDto> jiraIssues = estimationBlock.getEstimateIssues()
                        .stream().filter(estimateIssue -> estimateIssue.getIssueMetadata().getProvider().equals(ServiceType.SCRUMLY.toString()))
                        .map(estimateIssue -> ExportIssueDto.IssueChangesDto.builder()
                                .issueId(estimateIssue.getIssueMetadata().getIssueKey())
                                .estimation(estimateIssue.getFinalEstimate())
                                .build())
                        .toList();
                internalExportIssueDto = ExportIssueDto.builder()
                        .connectingId(activityRoom.getTeamMetadata().getTeamId())
                        .provider(ServiceType.SCRUMLY)
                        .issues(jiraIssues)
                        .activityId(activityRoom.getActivityId())
                        .activityName(activityRoom.getActivityName())
                        .build();
            }
            if (internalExportIssueDto != null && !internalExportIssueDto.getIssues().isEmpty()) {
                messageProducer.sendExportIssueEvent(TopicConfiguration.QUEUE_EXPORT_SCRUMLY_ISSUE, internalExportIssueDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<SyncBlockOption> getSyncBlockOptions(String activityId) {
        ActivityRoomEntity room = getActivityRoomEntity(activityId);
        ActivityDto activityDto = eventServiceFeignClient.getActivityById(activityId);
        if (!activityDto.getCreatedBy().equals(getUsername())) {
            throw new AccessDeniedException("You cannot start activity room ask activity creator");
        }

        ActivityTemplateDto templateDto = activityDto.getActivityTemplate();
        if (templateDto == null) {
            throw new EntityNotFoundException("Workspace template is null");
        }

        List<SyncBlockOption> syncBlockOptions = new ArrayList<>();

        try {
            List<ActivityDto> activityDtos = eventServiceFeignClient.getRecentTemplateActivities(activityDto.getTeamId(), templateDto.getTemplateId());
            syncBlockOptions = activityDtos.stream()
                    .filter(dto -> !dto.getActivityId().equals(activityId))
                    .map(activity -> SyncBlockOption.builder()
                            .activityId(activity.getActivityId())
                            .teamId(activity.getTeamId())
                            .eventTitle(activity.getScheduledEvent().getTitle())
                            .eventStartDateTime(activity.getScheduledEvent().getStartDateTime())
                            .eventEndDateTime(activity.getScheduledEvent().getEndDateTime())
                            .templateName(activity.getActivityTemplate().getName())
                            .templateId(activity.getActivityTemplate().getTemplateId())
                            .build())
                    .toList();
        } catch (Exception e) {
            throw new ServiceErrorException(e);
        }
        return syncBlockOptions;
    }

    @Override
    public void test(String activityId) {
        ActivityRoom activityRoom = getActivityRoomOrThrow(activityId);
        exportEstimateBlockChanges(activityRoom);
        exportItemBoardChanges(activityRoom);
    }

    @Override
    public ActivityRoom getActivityRoom(String activityId) {
        ActivityRoomEntity room = roomRepository.findActivityRoomByActivityId(activityId);
        return room != null
                ? deserializeMeetingRoom(room.getRoomPayload())
                : null;
    }

    @Override
    public ActivityRoom getActivityRoomOrThrow(String activityId) {
        ActivityRoomEntity activityRoom = getActivityRoomEntity(activityId);
        return deserializeMeetingRoom(activityRoom.getRoomPayload());
    }

    @Override
    public Boolean isActivityRoomCreated(String activityId) {
        return roomRepository.existsByActivityId(activityId);
    }

    @Override
    public void joinRoom(String activityId) {
        UserProfileDto user = userServiceFeignClient.findMyUserProfile().getBody();
        ActivityDto activityDto = eventServiceFeignClient.getActivityById(activityId);

        boolean isInvited = activityDto.getScheduledEvent().getAttendees().stream()
                .anyMatch(attendeeDto -> attendeeDto.getUserId().equals(user.getUserId()));
        if (!isInvited) {
            throw new AccessDeniedException("You are not invited into this workspace!");
        }

        ActivityRoomEntity room = getActivityRoomEntity(activityId);
        ActivityRoom activityRoom = getActivityRoomOrThrow(activityId);

        boolean isJoined = activityRoom.getJoinedUsers().stream()
                .anyMatch(userMetadata -> userMetadata.getUserId().equals(user.getUserId()));
        if (!isJoined) {
            ActivityRoom.UserMetadata userMetadata = businessMapper.userProfileToMetadata(user);
            activityRoom.getJoinedUsers().add(userMetadata);

            List<ActivityRoom.BlockNavigationMetadata.ActivityBlockUserNavigation> userNavigations =
                    activityRoom.getBlockNavigationMetadata().getUserNavigations();
            userNavigations.removeIf(activityBlockUserNavigation -> activityBlockUserNavigation.getUserId().equals(getUsername()));
            userNavigations.add(ActivityRoom.BlockNavigationMetadata.ActivityBlockUserNavigation.builder()
                                        .userId(getUsername())
                                        .activeBlockId(activityRoom.getBlockNavigationMetadata().getActiveBlockId())
                                        .build());
            room.setRoomPayload(serializeMeetingRoom(activityRoom));
            roomRepository.save(room);
        }

        notifyAll(activityRoom);
    }

    @Override
    public void exitRoom(String activityId) {
        UserProfileDto user = userServiceFeignClient.findMyUserProfile().getBody();
        ActivityRoomEntity room = getActivityRoomEntity(activityId);
        ActivityRoom activityRoom = getActivityRoomOrThrow(activityId);
        activityRoom.getJoinedUsers().removeIf(userMetadata -> userMetadata.getUserId().equals(user.getUserId()));
        room.setRoomPayload(serializeMeetingRoom(activityRoom));
        roomRepository.save(room);
        notifyAll(activityRoom);
    }

    @Override
    public void save(ActivityRoom activityRoom) {
        ActivityRoomEntity room = getActivityRoomEntity(activityRoom.getActivityId());
        room.setRoomPayload(serializeMeetingRoom(activityRoom));
        roomRepository.save(room);
    }

    @Override
    public void saveAndNotifyAll(ActivityRoom activityRoom) {
        ActivityRoomEntity room = getActivityRoomEntity(activityRoom.getActivityId());
        room.setRoomPayload(serializeMeetingRoom(activityRoom));
        roomRepository.save(room);
        notifyAll(activityRoom);
    }

    @Override
    public void saveAndNotifyUser(ActivityRoom activityRoom, String username) {
        ActivityRoomEntity room = getActivityRoomEntity(activityRoom.getActivityId());
        room.setRoomPayload(serializeMeetingRoom(activityRoom));
        roomRepository.save(room);
        notify(activityRoom, username);
    }

    @Override
    public void notify(ActivityRoom activityRoom, String userId) {
        ActivityRoom.UserMetadata joinedUser = activityRoom.getJoinedUsers()
                .stream().filter(userMetadata -> userMetadata.getUserId().equals(userId))
                .findAny()
                .orElse(null);
        activityRoom.setCurrentUser(joinedUser);
        webSocketService.sendActivityRoomToUsers(activityRoom, Arrays.asList(userId));
    }

    @Override
    public void notifyAll(ActivityRoom activityRoom) {
        for (ActivityRoom.UserMetadata joinedUser : activityRoom.getJoinedUsers()) {
            activityRoom.setCurrentUser(joinedUser);
            webSocketService.sendActivityRoomToUser(activityRoom, joinedUser.getUserId());
        }
    }

    @Override
    public void notifyTimerChange(ActivityRoom activityRoom, ActivityTimerState timerState) {
        for (ActivityRoom.UserMetadata joinedUser : activityRoom.getJoinedUsers()) {
            activityRoom.setCurrentUser(joinedUser);
            webSocketService.sendActivityTimerChange(activityRoom, joinedUser.getUserId(), timerState);
        }
    }

    @Override
    public void sendUserStatusChange(ActivityRoom activityRoom, String userId, UserConnectionStatus connectionStatus) {
        webSocketService.sendUserStatusChange(activityRoom, userId, connectionStatus);
    }
}
