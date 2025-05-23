package com.scrumly.eventservice.domain.activity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scrumly.eventservice.domain.ActivityTemplateEntity;
import com.scrumly.eventservice.domain.events.EventEntity;
import com.scrumly.eventservice.domain.workspace.WorkspaceEntity;
import com.scrumly.eventservice.enums.ActivityStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class ActivityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false, unique = true)
    private String activityId;
    @Column
    private String recurringEventId;
    @Column(nullable = false, updatable = false)
    private String teamId;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false, updatable = false)
    private String createdBy;
    @Column(nullable = false)
    private ActivityStatus status;
    @OneToOne
    private EventEntity scheduledEvent;
    @ManyToOne
    private WorkspaceEntity workspace;
    @ManyToOne
    private ActivityTemplateEntity activityTemplate;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ActivityHistory> historyLog;
    @Column
    private String roomId;
}
