package com.scrumly.eventservice.mapper;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.domain.ActivityTemplateEntity;
import com.scrumly.eventservice.domain.activity.ActivityEntity;
import com.scrumly.eventservice.domain.workspace.WorkspaceEntity;
import com.scrumly.eventservice.dto.ActivityBlockConfigDto;
import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.dto.ActivityTemplateDto;
import com.scrumly.eventservice.dto.activity.ActivityDto;
import com.scrumly.eventservice.dto.blocks.estimate.EstimateBlockDto;
import com.scrumly.eventservice.dto.blocks.itemsBoard.ItemsBoardBlockDto;
import com.scrumly.eventservice.dto.blocks.question.QuestionBlockDto;
import com.scrumly.eventservice.dto.blocks.reflect.ReflectBlockDto;
import com.scrumly.eventservice.dto.events.EventDto;
import com.scrumly.eventservice.dto.requests.CreateActivityBlockConfigRQ;
import com.scrumly.eventservice.dto.requests.CreateActivityBlockRQ;
import com.scrumly.eventservice.dto.requests.question.CreateQuestionBlockRQ;
import com.scrumly.eventservice.dto.requests.question.CreateQuestionRQ;
import com.scrumly.eventservice.dto.user.UserProfileDto;
import com.scrumly.eventservice.dto.workspace.WorkspaceDto;
import com.scrumly.eventservice.services.ActivityBlockService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.scrumly.eventservice.enums.ActivityBlockType.QUESTION_BLOCK;
import static com.scrumly.eventservice.enums.ActivityBlockType.REFLECT_BLOCK;

@Service
@RequiredArgsConstructor
public class BusinessMapper {
    private final ModelMapper modelMapper;
    private final ActivityBlockService activityBlockService;


    public ActivityTemplateDto activityTemplateToDtoInfo(ActivityTemplateEntity template) {
        return modelMapper.map(template, ActivityTemplateDto.class);
    }

    public ActivityTemplateDto activityTemplateToDto(ActivityTemplateEntity template) {
        ActivityTemplateDto templateDto = modelMapper.map(template, ActivityTemplateDto.class);
        for (ActivityBlockConfigDto block : templateDto.getBlocks()) {
            ActivityBlockEntity blockEntity = activityBlockService.findActivityBlock(block.getBlockId(), block.getBlockType());
            ActivityBlockDto blockDto = switch (blockEntity.getType()) {
                case QUESTION_BLOCK -> modelMapper.map(blockEntity, QuestionBlockDto.class);
                case REFLECT_BLOCK -> modelMapper.map(blockEntity, ReflectBlockDto.class);
                case ESTIMATE_BLOCK -> modelMapper.map(blockEntity, EstimateBlockDto.class);
                case ITEM_BOARD_BLOCK -> modelMapper.map(blockEntity, ItemsBoardBlockDto.class);
                default -> modelMapper.map(blockEntity, ActivityBlockDto.class);
            };
            block.setBlockDto(blockDto);
        }
        return templateDto;
    }


    public ActivityDto activityToDto(ActivityEntity activity) {
        return activityToDto(activity, false, true);
    }

    public ActivityDto activityToDto(ActivityEntity activity,
                                     boolean skipWorkspace,
                                     boolean skipActivities) {
        return ActivityDto.builder()
                .id(activity.getId())
                .activityId(activity.getActivityId())
                .teamId(activity.getTeamId())
                .recurringEventId(activity.getRecurringEventId())
                .createdAt(activity.getCreatedAt())
                .createdBy(activity.getCreatedBy())
                .status(activity.getStatus())
                .scheduledEvent(activity.getScheduledEvent() != null
                                        ? modelMapper.map(activity.getScheduledEvent(), EventDto.class)
                                        : null)
                .activityTemplate(activity.getActivityTemplate() != null
                                          ? modelMapper.map(activity.getActivityTemplate(), ActivityTemplateDto.class)
                                          : null)
                .workspace(!skipWorkspace
                                   ? workspaceToEntity(activity.getWorkspace(), skipActivities)
                                   : null)
                .build();
    }

    public ActivityDto activityToDtoWithoutActivity(ActivityEntity activity) {
        return ActivityDto.builder()
                .id(activity.getId())
                .activityId(activity.getActivityId())
                .teamId(activity.getTeamId())
                .recurringEventId(activity.getRecurringEventId())
                .createdAt(activity.getCreatedAt())
                .createdBy(activity.getCreatedBy())
                .status(activity.getStatus())
                .scheduledEvent(activity.getScheduledEvent() != null
                                        ? modelMapper.map(activity.getScheduledEvent(), EventDto.class)
                                        : null)
                .activityTemplate(activity.getActivityTemplate() != null
                                          ? modelMapper.map(activity.getActivityTemplate(), ActivityTemplateDto.class)
                                          : null)
                .workspace(activity.getWorkspace() != null
                                   ? workspaceToEntityWithoutActivity(activity.getWorkspace())
                                   : null)
                .build();
    }

    public ActivityDto activityToDtoWithoutWorkspace(ActivityEntity activity) {
        return ActivityDto.builder()
                .id(activity.getId())
                .activityId(activity.getActivityId())
                .teamId(activity.getTeamId())
                .recurringEventId(activity.getRecurringEventId())
                .createdAt(activity.getCreatedAt())
                .createdBy(activity.getCreatedBy())
                .status(activity.getStatus())
                .scheduledEvent(activity.getScheduledEvent() != null
                                        ? modelMapper.map(activity.getScheduledEvent(), EventDto.class)
                                        : null)
                .activityTemplate(activity.getActivityTemplate() != null
                                          ? activityTemplateToDto(activity.getActivityTemplate())
                                          : null)
                .build();
    }

    public WorkspaceDto workspaceToEntity(WorkspaceEntity workspace,
                                          boolean skipActivities) {
        return WorkspaceDto.builder()
                .id(workspace.getId())
                .workspaceId(workspace.getWorkspaceId())
                .conferenceId(workspace.getConferenceId())
                .createdAt(workspace.getCreatedAt())
                .createdBy(workspace.getCreatedBy())
                .activities(!skipActivities ?
                                    workspace.getActivities().stream()
                                            .map(activity -> activityToDto(activity, true, skipActivities))
                                            .collect(Collectors.toList())
                                    : null)
                .build();
    }

    public WorkspaceDto workspaceToDtoWithActivities(WorkspaceEntity workspace) {
        return WorkspaceDto.builder()
                .id(workspace.getId())
                .workspaceId(workspace.getWorkspaceId())
                .conferenceId(workspace.getConferenceId())
                .createdAt(workspace.getCreatedAt())
                .createdBy(workspace.getCreatedBy())
                .activities(workspace.getActivities() != null
                                    ? workspace.getActivities().stream().map(this::activityToDtoWithoutWorkspace).toList()
                                    : new ArrayList<>())
                .build();
    }

    public WorkspaceDto workspaceToEntityWithoutActivity(WorkspaceEntity workspace) {
        return WorkspaceDto.builder()
                .id(workspace.getId())
                .workspaceId(workspace.getWorkspaceId())
                .conferenceId(workspace.getConferenceId())
                .createdAt(workspace.getCreatedAt())
                .createdBy(workspace.getCreatedBy())
                .build();
    }
}
