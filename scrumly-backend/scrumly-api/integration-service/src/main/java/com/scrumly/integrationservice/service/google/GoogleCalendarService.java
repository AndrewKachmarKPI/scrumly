package com.scrumly.integrationservice.service.google;

import com.google.api.services.calendar.model.Events;
import com.scrumly.integrationservice.dto.ServiceAuthorizeRQ;
import com.scrumly.integrationservice.dto.ServiceCredentialsDto;
import com.scrumly.integrationservice.dto.googleCalendar.CreateGoogleCalendarEventDto;
import com.scrumly.integrationservice.dto.googleCalendar.GetGoogleCalendarEventsDto;
import com.scrumly.integrationservice.dto.googleCalendar.GoogleCalendarEventDto;

public interface GoogleCalendarService {
    ServiceCredentialsDto authorize(ServiceAuthorizeRQ authorizeRQ);
    String getAuthorizationUrl();
    Events getCalendarEventList(GetGoogleCalendarEventsDto dto);

    GoogleCalendarEventDto createCalendarEvent(CreateGoogleCalendarEventDto createGoogleCalendarEventDto);
    GoogleCalendarEventDto updateCalendarEvent(String eventId, CreateGoogleCalendarEventDto createGoogleCalendarEventDto);

    void deleteCalendarEvent(String eventId);

    void deleteCalendarRecurrentEvent(String eventId, String recurrentEventId);
}
