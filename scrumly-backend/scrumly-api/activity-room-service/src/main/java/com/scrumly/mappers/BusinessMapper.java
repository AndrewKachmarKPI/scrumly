
package com.scrumly.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scrumly.domain.ActivityRoom;
import com.scrumly.domain.ActivityRoomEntity;
import com.scrumly.domain.ActivityRoomReport;
import com.scrumly.dto.backlog.IssueDto;
import com.scrumly.dto.issues.IssueShortInfo;
import com.scrumly.dto.user.UserProfileDto;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.event.dto.ActivityBlockConfigDto;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.integration.jiraCloud.GetIssuePickerSuggestions;
import com.scrumly.integration.jiraCloud.GetSearchIssuesEnhanced;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessMapper {
    private final ModelMapper modelMapper;

    public ActivityRoom.UserMetadata userProfileToMetadata(UserProfileDto userProfileDto) {
        return ActivityRoom.UserMetadata.builder()
                .userId(userProfileDto.getUserId())
                .email(userProfileDto.getEmail())
                .firstName(userProfileDto.getFirstName())
                .lastName(userProfileDto.getLastName())
                .avatarId(userProfileDto.getAvatarId())
                .build();
    }

    public ActivityRoom.BlockNavigationMetadata.ActivityBlockMetadata blockConfigDtoToBlockMetadata(ActivityBlockConfigDto blockConfigDto) {
        return ActivityRoom.BlockNavigationMetadata.ActivityBlockMetadata.builder()
                .id(blockConfigDto.getBlockDto().getBlockId())
                .order(blockConfigDto.getBlockOrder())
                .type(blockConfigDto.getBlockType().toString())
                .name(blockConfigDto.getBlockDto().getName())
                .description(blockConfigDto.getBlockDto().getDescription())
                .build();
    }

    public List<IssueShortInfo> getIssueShortInfo(GetIssuePickerSuggestions suggestions, ServiceType serviceType) {
        return suggestions.getSections().stream()
                .flatMap(section -> section.getIssues().stream())
                .map(issue -> IssueShortInfo.builder()
                        .issueId(String.valueOf(issue.getId()))
                        .imgPath(issue.getImg())
                        .title(issue.getSummaryText())
                        .issueKey(issue.getKey())
                        .issueUrl(issue.getUrl())
                        .provider(serviceType)
                        .build()).toList();
    }

    public List<IssueShortInfo> getIssueShortInfoEnhances(GetSearchIssuesEnhanced searchIssuesEnhanced, ServiceType serviceType) {
        return searchIssuesEnhanced.getIssues().stream()
                .map(issue -> IssueShortInfo.builder()
                        .issueId(String.valueOf(issue.getId()))
                        .imgPath(issue.getFields().getIssueType() != null
                                         ? issue.getFields().getIssueType().getIconUrl()
                                         : "")
                        .title(issue.getFields().getSummary())
                        .issueKey(issue.getKey())
                        .issueUrl(issue.getUrl())
                        .provider(serviceType)
                        .projectId(issue.getFields().getProject() != null
                                           ? issue.getFields().getProject().getId()
                                           : null)
                        .projectName(issue.getFields().getProject() != null
                                             ? issue.getFields().getProject().getName()
                                             : "")
                        .build()).toList();
    }

    public List<IssueShortInfo> getIssueShortInfo(List<IssueDto> issueDtoList) {
        return issueDtoList.stream()
                .map(issue -> IssueShortInfo.builder()
                        .issueId(String.valueOf(issue.getId()))
                        .issueKey(issue.getIssueKey())
                        .title(issue.getTitle())
                        .imgPath(issue.getIssueType().getIconUrl())
                        .issueUrl(issue.getIssueKey())
                        .projectName(issue.getBacklogName())
                        .projectId(issue.getBacklogId())
                        .provider(ServiceType.SCRUMLY)
                        .estimate(issue.getIssueEstimation() != null
                                          ? issue.getIssueEstimation().getEstimation()
                                          : null)
                        .build()).toList();
    }

    public static String serializeMeetingRoom(ActivityRoom activityRoom) {
        if (activityRoom == null) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerSubtypes(new NamedType(ActivityRoom.ActivityBlock.ActivityQuestionBlock.class, "ActivityQuestionBlock"));
            mapper.registerSubtypes(new NamedType(ActivityRoom.ActivityBlock.ActivityReflectBlock.class, "ActivityReflectBlock"));
            mapper.registerSubtypes(new NamedType(ActivityRoom.ActivityBlock.ActivityItemBoardBlock.class, "ActivityItemBoardBlock"));
            mapper.registerSubtypes(new NamedType(ActivityRoom.ActivityBlock.ActivityEstimateBlock.class, "ActivityEstimateBlock"));
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            FilterProvider filters = getFilterProvider();
            return mapper
                    .writer(filters)
                    .writeValueAsString(activityRoom);
        } catch (Exception e) {
            throw new ServiceErrorException(e);
        }
    }

    public static ActivityRoom deserializeMeetingRoom(String roomPayload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.readValue(roomPayload, ActivityRoom.class);
        } catch (Exception e) {
            throw new ServiceErrorException(e);
        }
    }

    public static ActivityRoomReport deserializeMeetingRoomReport(String reportPayload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.readValue(reportPayload, ActivityRoomReport.class);
        } catch (Exception e) {
            throw new ServiceErrorException(e);
        }
    }


    private static FilterProvider getFilterProvider() {
        return new SimpleFilterProvider()
                .addFilter("ActivityRoomFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("ActivityRoomStatusMetadataFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("TeamMetadataFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("ActivityTemplateMetadataFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("UserMetadataFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("QuestionMetadata", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("ReflectColumnCardFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("ActivityItemBoardBlockFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("TeamLoadMetadataFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("ItemBoardColumnMetadataFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("ItemStatusMetadataFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("ItemBoardColumnIssuesFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("ItemBoardColumnUserIssueFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("ActivityEstimateBlockFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("EstimateScaleMetadataFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("EstimateIssueFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("IssueMetadataFilter", SimpleBeanPropertyFilter.serializeAll())
                .addFilter("QuestionMetadataFilter", SimpleBeanPropertyFilter.serializeAll());
    }

    public static String serializeMeetingRoomReport(ActivityRoomReport activityRoomReport) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.registerSubtypes(new NamedType(ActivityRoomReport.ActivityBlockReport.ActivityQuestionBlockReport.class, "ActivityQuestionBlockReport"));
            mapper.registerSubtypes(new NamedType(ActivityRoomReport.ActivityBlockReport.ActivityItemBoardBlockReport.class, "ActivityItemBoardBlockReport"));
            mapper.registerSubtypes(new NamedType(ActivityRoomReport.ActivityBlockReport.ActivityReflectBlockReport.class, "ActivityReflectBlockReport"));
            mapper.registerSubtypes(new NamedType(ActivityRoomReport.ActivityBlockReport.ActivityEstimateBlockReport.class, "ActivityEstimateBlockReport"));
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            FilterProvider filters = getFilterProvider();
            return mapper
                    .writer(filters)
                    .writeValueAsString(activityRoomReport);
        } catch (Exception e) {
            throw new ServiceErrorException(e);
        }
    }

    public static String serializeMeetingRoomWithoutIds(ActivityRoom activityRoom) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter("ActivityRoomFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("id", "activityId", "notesMetadata", "blockNavigationMetadata"))
                    .addFilter("ActivityRoomStatusMetadataFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("currentStatus", "isActive", "statusChangeMetadata"))
                    .addFilter("TeamMetadataFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("organizationId", "teamId"))
                    .addFilter("ActivityTemplateMetadataFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("activityId"))
                    .addFilter("UserMetadataFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("userId", "avatarId"))
                    .addFilter("QuestionMetadata", SimpleBeanPropertyFilter
                            .serializeAllExcept("id"))
                    .addFilter("ReflectColumnCardFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("version", "order"))
                    .addFilter("ActivityItemBoardBlockFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("issueBacklog", "teamMembers"))
                    .addFilter("TeamLoadMetadataFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("doneStatuses", "inProgressStatuses"))
                    .addFilter("ItemBoardColumnMetadataFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("columnId", "cardId", "order", "columnIssueStatuses"))
                    .addFilter("ItemStatusMetadataFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("statusId", "backlogId"))
                    .addFilter("ItemBoardColumnIssuesFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("version"))
                    .addFilter("ItemBoardColumnUserIssueFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("columnId", "cardId", "order"))
                    .addFilter("ActivityEstimateBlockFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("activeEstimateIssueId"))
                    .addFilter("EstimateScaleMetadataFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("activeEstimateIssueId"))
                    .addFilter("EstimateIssueFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("id", "isRevealed"))
                    .addFilter("IssueMetadataFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("issueId", "imgPath", "issueUrl", "projectId"))
                    .addFilter("QuestionMetadataFilter", SimpleBeanPropertyFilter
                            .serializeAllExcept("id"));
            return mapper
                    .writer(filters)
                    .writeValueAsString(activityRoom);
        } catch (Exception e) {
            throw new ServiceErrorException(e);
        }
    }
}
