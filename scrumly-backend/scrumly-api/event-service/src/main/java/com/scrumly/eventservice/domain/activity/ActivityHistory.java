package com.scrumly.eventservice.domain.activity;

import com.scrumly.eventservice.domain.ActivityTemplateEntity;
import com.scrumly.eventservice.domain.events.EventEntity;
import com.scrumly.eventservice.enums.ActivityStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class ActivityHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateTime;
    @Column(nullable = false, updatable = false)
    private String performedBy;
    @Column
    private ActivityStatus previousStatus;
    @Column
    private ActivityStatus newStatus;
    @Column
    private String changeDetails;
}
