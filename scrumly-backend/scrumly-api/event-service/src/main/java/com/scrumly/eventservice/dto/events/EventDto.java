package com.scrumly.eventservice.dto.events;

import com.scrumly.eventservice.domain.events.EventRecurrenceEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {
    private Long id;
    private String eventId;
    private String createdFor;
    private String createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private String title;
    private String location;
    private String description;
    private String startDateTime;
    private String endDateTime;
    private String startTimeZone;
    private String endTimeZone;
    private EventRecurrenceDto recurrence;
    private EventMetadataDto eventMetadata;
    private List<EventAttendeeDto> attendees;
    private boolean createConference;
    private boolean active;
    private Long duration;


    public EventDto(EventDto other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null EventDto");
        }
        this.id = other.id;
        this.eventId = other.eventId;
        this.createdFor = other.createdFor;
        this.createdBy = other.createdBy;
        this.createdByName = other.createdByName;
        this.createdAt = other.createdAt;
        this.title = other.title;
        this.location = other.location;
        this.description = other.description;
        this.startDateTime = other.startDateTime;
        this.endDateTime = other.endDateTime;
        this.startTimeZone = other.startTimeZone;
        this.endTimeZone = other.endTimeZone;
        this.recurrence = other.recurrence != null
                ? new EventRecurrenceDto(other.recurrence)
                : null;
        this.eventMetadata = other.eventMetadata != null
                ? new EventMetadataDto(other.eventMetadata)
                : null;
        this.attendees = other.attendees != null
                ? new ArrayList<>(other.attendees)
                : null;
        this.createConference = other.createConference;
        this.active = other.active;
        this.duration = other.duration;
    }
}
