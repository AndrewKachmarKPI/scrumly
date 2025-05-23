package com.scrumly.eventservice.dto.activity;

import com.scrumly.eventservice.dto.ActivityTemplateDto;
import com.scrumly.eventservice.dto.events.EventDto;
import com.scrumly.eventservice.dto.workspace.WorkspaceDto;
import com.scrumly.eventservice.enums.ActivityStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class ActivityDto {
    private Long id;
    private String activityId;
    private String teamId;
    private String recurringEventId;
    private LocalDateTime createdAt;
    private String createdBy;
    private ActivityStatus status;
    private EventDto scheduledEvent;
    private ActivityTemplateDto activityTemplate;
    private WorkspaceDto workspace;
}
