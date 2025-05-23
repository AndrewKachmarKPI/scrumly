package com.scrumly.eventservice.dto.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventMetadataDto {
    private Long id;
    private String calendarProvider;
    private String conferenceProvider;
    private String calendarEventId;
    private String calendarEventUID;
    private String calendarEventLink;
    private String conferenceLink;

    public EventMetadataDto(EventMetadataDto other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null EventMetadataDto");
        }
        this.id = other.id;
        this.calendarProvider = other.calendarProvider;
        this.conferenceProvider = other.conferenceProvider;
        this.calendarEventId = other.calendarEventId;
        this.calendarEventUID = other.calendarEventUID;
        this.calendarEventLink = other.calendarEventLink;
        this.conferenceLink = other.conferenceLink;
    }
}
