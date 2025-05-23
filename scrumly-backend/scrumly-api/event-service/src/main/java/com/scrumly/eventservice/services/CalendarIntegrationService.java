package com.scrumly.eventservice.services;

import com.scrumly.dto.googleCalendar.GoogleCalendarEventDto;
import com.scrumly.eventservice.dto.events.EventDto;
import com.scrumly.eventservice.dto.requests.events.CreateActivityRQ;

public interface CalendarIntegrationService {
    GoogleCalendarEventDto createGoogleCalendarEvent(CreateActivityRQ rq);
    GoogleCalendarEventDto updateGoogleCalendarEvent(String eventId, EventDto rq);
    void deleteGoogleCalendarEvent(String eventId);

    void deleteGoogleCalendarRecurrentEvent(String eventId, String recurrenceEventId);
}
