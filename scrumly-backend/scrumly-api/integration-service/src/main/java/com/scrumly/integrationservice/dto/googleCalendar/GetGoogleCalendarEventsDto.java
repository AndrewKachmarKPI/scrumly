package com.scrumly.integrationservice.dto.googleCalendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class GetGoogleCalendarEventsDto {
    private final String startDate;
    private final String endDate;
    private final String query;
    private final String orderBy;
    private final Boolean singleEvents;
}
