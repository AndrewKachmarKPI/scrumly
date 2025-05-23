package com.scrumly.eventservice.domain.events;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false, unique = true)
    private String eventId;
    @Column(nullable = false, updatable = false)
    private String createdFor;
    @Column(nullable = false, updatable = false)
    private String createdBy;
    @Column(nullable = false, updatable = false)
    private String createdByName;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private String title;
    @Column
    private String location;
    @Column
    private Boolean active;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private String startDateTime;
    @Column(nullable = false)
    private String startTimeZone;
    @Column(nullable = false)
    private String endDateTime;
    @Column(nullable = false)
    private String endTimeZone;
    @Column
    private Boolean isCreateConference;
    @Column
    private Long duration;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private EventRecurrenceEntity recurrence;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private EventMetadataEntity eventMetadata;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<EventAttendeeEntity> attendees;

    public EventEntity(EventEntity other) {
        this.eventId = other.eventId;
        this.createdFor = other.createdFor;
        this.createdBy = other.createdBy;
        this.createdByName = other.createdByName;
        this.createdAt = other.createdAt;
        this.title = other.title;
        this.location = other.location;
        this.active = other.active;
        this.description = other.description;
        this.startDateTime = other.startDateTime;
        this.startTimeZone = other.startTimeZone;
        this.endDateTime = other.endDateTime;
        this.endTimeZone = other.endTimeZone;
        this.isCreateConference = other.isCreateConference;
        this.duration = other.duration;
        this.recurrence = other.recurrence != null ? new EventRecurrenceEntity(other.recurrence) : null;
        this.eventMetadata = other.eventMetadata != null ? new EventMetadataEntity(other.eventMetadata) : null;
        this.attendees = other.attendees != null ? other.attendees.stream()
                .map(EventAttendeeEntity::new)
                .collect(Collectors.toList()) : null;
    }
}
