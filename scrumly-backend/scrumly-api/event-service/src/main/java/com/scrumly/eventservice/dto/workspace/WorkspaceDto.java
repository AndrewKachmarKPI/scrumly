package com.scrumly.eventservice.dto.workspace;

import com.scrumly.eventservice.domain.activity.ActivityEntity;
import com.scrumly.eventservice.dto.activity.ActivityDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode
public class WorkspaceDto {
    private Long id;
    private String workspaceId;
    private String conferenceId;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<ActivityDto> activities;
}
