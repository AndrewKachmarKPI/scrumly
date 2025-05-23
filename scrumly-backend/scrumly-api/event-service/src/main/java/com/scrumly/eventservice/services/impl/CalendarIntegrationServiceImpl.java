package com.scrumly.eventservice.services.impl;

import com.scrumly.dto.googleCalendar.CreateGoogleCalendarEventDto;
import com.scrumly.dto.googleCalendar.GoogleCalendarEventDto;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.eventservice.dto.events.EventDto;
import com.scrumly.eventservice.dto.requests.events.CreateActivityRQ;
import com.scrumly.eventservice.feign.IntegrationServiceFeignClient;
import com.scrumly.eventservice.services.CalendarIntegrationService;
import com.scrumly.exceptions.types.ServiceErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalendarIntegrationServiceImpl implements CalendarIntegrationService {
    private final IntegrationServiceFeignClient integrationServiceFeignClient;


    @Override
    public GoogleCalendarEventDto createGoogleCalendarEvent(CreateActivityRQ createActivityRQ) {
        EventDto eventDto = createActivityRQ.getEventDto();
        ServiceType serviceType = ServiceType.valueOf(createActivityRQ.getCalendarProvider());
        boolean isConnected = Boolean.TRUE.equals(integrationServiceFeignClient.isConnected(serviceType).getBody());
        if (!isConnected) {
            throw new ServiceErrorException("Cannot schedule event user is not authorized with: " + createActivityRQ.getCalendarProvider());
        }
        CreateGoogleCalendarEventDto rq = getCreateGoogleCalendarEventDto(eventDto);
        return sendCreateGoogleCalendarEvent(rq).getBody();
    }

    private static CreateGoogleCalendarEventDto getCreateGoogleCalendarEventDto(EventDto eventDto) {
        return CreateGoogleCalendarEventDto.builder()
                .summary(eventDto.getTitle())
                .location(eventDto.getLocation())
                .description(eventDto.getDescription())
                .startDateTime(eventDto.getStartDateTime())
                .startTimeZone(eventDto.getStartTimeZone())
                .endDateTime(eventDto.getEndDateTime())
                .endTimeZone(eventDto.getEndTimeZone())
                .recurrence(eventDto.getRecurrence() != null
                        ? Collections.singletonList(eventDto.getRecurrence().getRecurrence())
                        : new ArrayList<>())
                .attendees(eventDto.getAttendees().stream()
                        .map(eventAttendeeDto -> CreateGoogleCalendarEventDto.AttendeeDto.builder()
                                .displayName(eventAttendeeDto.getDisplayName())
                                .emailAddress(eventAttendeeDto.getUserEmailAddress())
                                .build()).collect(Collectors.toList()))
                .isCreateConference(eventDto.isCreateConference())
                .build();
    }

    @Override
    public GoogleCalendarEventDto updateGoogleCalendarEvent(String eventId, EventDto rq) {
        CreateGoogleCalendarEventDto createRq = getCreateGoogleCalendarEventDto(rq);
        return sendUpdateGoogleCalendarEvent(eventId, createRq).getBody();
    }

    @Override
    public void deleteGoogleCalendarEvent(String eventId) {
        sendDeleteCalendarEvent(eventId);
    }

    @Override
    public void deleteGoogleCalendarRecurrentEvent(String eventId, String recurrenceEventId) {
        sendDeleteCalendarRecurrentEvent(eventId, recurrenceEventId);
    }

    @Retryable(
            value = {ServiceErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public ResponseEntity<GoogleCalendarEventDto> sendCreateGoogleCalendarEvent(CreateGoogleCalendarEventDto rq) {
        try {
            return integrationServiceFeignClient.createGoogleCalendarEvent(rq);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Retryable(
            value = {ServiceErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public ResponseEntity<GoogleCalendarEventDto> sendUpdateGoogleCalendarEvent(String eventId, CreateGoogleCalendarEventDto rq) {
        try {
            return integrationServiceFeignClient.updateCalendarEvent(eventId, rq);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Retryable(
            value = {ServiceErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void sendDeleteCalendarEvent(String eventId) {
        try {
            integrationServiceFeignClient.deleteGoogleCalendarEvent(eventId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }

    @Retryable(
            value = {ServiceErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void sendDeleteCalendarRecurrentEvent(String eventId, String recurrenceEventId) {
        try {
            integrationServiceFeignClient.deleteGoogleCalendarRecurrentEvent(eventId, recurrenceEventId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
    }


    @Recover
    public ResponseEntity<GoogleCalendarEventDto> recover(ServiceErrorException e, CreateGoogleCalendarEventDto rq) {
        log.error("Failed to create Google Calendar event after retries", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}
