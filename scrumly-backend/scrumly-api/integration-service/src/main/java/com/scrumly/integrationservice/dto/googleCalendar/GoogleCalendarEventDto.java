package com.scrumly.integrationservice.dto.googleCalendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class GoogleCalendarEventDto {
    private final String calendarEventId;
    private final String calendarEventUID;
    private final String eventLink;
    private final String conferenceLink;
    private final String recurringEventId;
}
