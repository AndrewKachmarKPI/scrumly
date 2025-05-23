package com.scrumly.eventservice.domain.events;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class EventMetadataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String calendarProvider;
    @Column
    private String conferenceProvider;
    @Column(nullable = false, updatable = false)
    private String calendarEventId;
    @Column(nullable = false, updatable = false)
    private String calendarEventUID;
    @Column(nullable = false, updatable = false)
    private String calendarEventLink;
    @Column
    private String conferenceLink;
    @Column
    private String recurringEventId;

    public EventMetadataEntity(EventMetadataEntity other) {
        this.calendarProvider = other.calendarProvider;
        this.conferenceProvider = other.conferenceProvider;
        this.calendarEventId = other.calendarEventId;
        this.calendarEventUID = other.calendarEventUID;
        this.calendarEventLink = other.calendarEventLink;
        this.conferenceLink = other.conferenceLink;
        this.recurringEventId = other.recurringEventId;
    }
}
