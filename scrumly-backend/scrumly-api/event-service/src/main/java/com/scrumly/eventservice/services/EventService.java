package com.scrumly.eventservice.services;

import com.scrumly.eventservice.domain.events.EventEntity;
import com.scrumly.eventservice.dto.events.EventDto;
import com.scrumly.eventservice.dto.requests.events.CreateActivityRQ;
import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;

import java.util.List;

public interface EventService {
    List<EventDto> getAllEvents();

    List<EventDto> getAllEvents(String createdFor);

    PageDto<EventDto> findEvents(SearchQuery searchQuery);

    EventDto getEventById(String eventId);

    List<EventDto> createEvent(CreateActivityRQ createActivityRQ);

    List<EventEntity> scheduleEvent(CreateActivityRQ createActivityRQ);

    EventDto updateEvent(String eventId, EventDto eventDTO);

    EventDto scheduleCalendarEvent(String eventId, CreateActivityRQ rq);

    void cancelEvent(String eventId);

    void cancelEvents(List<String> eventId);

    void cancelRecurrentEvent(String eventId, String recurrenceEventId);

    void cancelAllRecurrentEvent(String recurrenceEventId);

    void deleteEvent(String eventId);

    void deleteEvents(List<String> events);
}
