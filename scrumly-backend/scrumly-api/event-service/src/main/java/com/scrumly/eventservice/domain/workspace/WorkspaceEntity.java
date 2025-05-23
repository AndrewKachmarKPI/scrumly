package com.scrumly.eventservice.domain.workspace;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.scrumly.eventservice.domain.activity.ActivityEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode
@Entity
public class WorkspaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false, unique = true)
    private String workspaceId;
    @Column
    private String conferenceId;
    @Column(nullable = false)
    private String createdBy;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @OneToMany(fetch = FetchType.LAZY)
    @JsonBackReference
    private List<ActivityEntity> activities = new ArrayList<>();
}
