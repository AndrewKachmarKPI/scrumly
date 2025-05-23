package com.scrumly.eventservice.services.impl;

import com.scrumly.dto.googleCalendar.GoogleCalendarEventDto;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.eventservice.domain.events.EventAttendeeEntity;
import com.scrumly.eventservice.domain.events.EventEntity;
import com.scrumly.eventservice.domain.events.EventMetadataEntity;
import com.scrumly.eventservice.domain.events.EventRecurrenceEntity;
import com.scrumly.eventservice.dto.events.EventAttendeeDto;
import com.scrumly.eventservice.dto.events.EventDto;
import com.scrumly.eventservice.dto.requests.events.CreateActivityRQ;
import com.scrumly.eventservice.dto.user.UserProfileDto;
import com.scrumly.eventservice.feign.IntegrationServiceFeignClient;
import com.scrumly.eventservice.feign.UserServiceFeignClient;
import com.scrumly.eventservice.repository.EventRepository;
import com.scrumly.eventservice.services.ActivityTemplateService;
import com.scrumly.eventservice.services.CalendarIntegrationService;
import com.scrumly.eventservice.services.EventService;
import com.scrumly.eventservice.utils.RecurrenceRuleParser;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.specification.GeneralSpecification;
import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.scrumly.eventservice.utils.SecurityUtils.getUsername;
import static com.scrumly.eventservice.utils.TimeUtils.getDuration;
import static com.scrumly.eventservice.utils.TimeUtils.getRecurrentEventId;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final UserServiceFeignClient userServiceFeignClient;
    private final ActivityTemplateService templateService;
    private final IntegrationServiceFeignClient integrationServiceFeignClient;
    private final CalendarIntegrationService calendarIntegrationService;

    @Override
    public EventDto getEventById(String eventId) {
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<EventDto> createEvent(CreateActivityRQ createActivityRQ) {
        return scheduleEvent(createActivityRQ).stream()
                .map(event -> modelMapper.map(event, EventDto.class))
                .toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<EventEntity> scheduleEvent(CreateActivityRQ createActivityRQ) {
        List<EventEntity> events = new ArrayList<>();
        UserProfileDto profileDto = userServiceFeignClient.findMyUserProfile().getBody();
        EventDto eventDto = createActivityRQ.getEventDto();
        EventEntity eventEntity = modelMapper.map(eventDto, EventEntity.class);
        String eventId = null;
        try {
            try {
                if (createActivityRQ.isCreateCalendarEvent()) {
                    ServiceType serviceType = ServiceType.valueOf(createActivityRQ.getCalendarProvider());
                    boolean isConnected = Boolean.TRUE.equals(integrationServiceFeignClient.isConnected(serviceType).getBody());
                    if (!isConnected) {
                        throw new ServiceErrorException("You are not connected with service selected");
                    }
                    includeCreatorAsAttendee(createActivityRQ);
                    EventMetadataEntity metadataEntity = createCalendarEvent(createActivityRQ);
                    eventEntity.setEventMetadata(metadataEntity);
                    eventId = metadataEntity.getCalendarEventId();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (profileDto != null) {
                eventEntity.getAttendees().add(EventAttendeeEntity.builder()
                        .displayName(profileDto.getFirstName() + " " + profileDto.getLastName())
                        .userId(profileDto.getUserId())
                        .userEmailAddress(profileDto.getEmail())
                        .build());
            }
            eventEntity.setCreatedAt(LocalDateTime.now());
            eventEntity.setCreatedBy(profileDto.getUserId());
            eventEntity.setCreatedByName(profileDto.getFirstName() + " " + profileDto.getLastName());
            eventEntity.setEventId(UUID.randomUUID().toString());
            eventEntity.setDuration(getDuration(eventEntity.getStartDateTime(), eventEntity.getEndDateTime()).toMinutes());
            eventEntity.setActive(true);
            if (eventDto.getRecurrence() != null) {
                String recurringEventId = eventEntity.getEventMetadata() != null
                        ? eventEntity.getEventMetadata().getRecurringEventId()
                        : UUID.randomUUID().toString();
                eventEntity.setRecurrence(EventRecurrenceEntity.builder()
                        .recurringEventId(recurringEventId)
                        .recurrence(eventDto.getRecurrence().getRecurrence())
                        .recurrenceText(eventDto.getRecurrence().getRecurrenceText())
                        .build());
            }
            eventEntity = eventRepository.save(eventEntity);
            events.add(eventEntity);
            if (eventDto.getRecurrence() != null) {
                List<EventEntity> recurrentEvents = getNewRecurrentEvents(eventEntity);
                events.addAll(eventRepository.saveAll(recurrentEvents));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (eventId != null) {
                deleteCalendarEvent(eventId, ServiceType.valueOf(createActivityRQ.getCalendarProvider()));
            }
            throw new ServiceErrorException(e);
        }
        return events;
    }

    public List<EventEntity> getNewRecurrentEvents(EventEntity event) {
        List<EventEntity> childEvents = new ArrayList<>();
        String recurrence = event.getRecurrence().getRecurrence();
        try {
            List<ZonedDateTime> startDates = RecurrenceRuleParser.parseRecurrence(recurrence, event.getStartDateTime())
                    .stream()
                    .map(dt -> ZonedDateTime.of(dt, ZonedDateTime.parse(event.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME).getZone()))
                    .toList();
            ZonedDateTime originalStart = ZonedDateTime.parse(event.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME);
            ZonedDateTime originalEnd = ZonedDateTime.parse(event.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME);
            Duration duration = Duration.between(originalStart, originalEnd);
            for (ZonedDateTime startDate : startDates) {
                ZonedDateTime endDate = startDate.plus(duration);
                EventEntity newEvent = new EventEntity(event);
                newEvent.setEventId(UUID.randomUUID().toString());
                newEvent.setStartDateTime(startDate.toString());
                newEvent.setEndDateTime(endDate.toString());
                if (newEvent.getEventMetadata() != null && newEvent.getRecurrence() != null) {
                    EventMetadataEntity metadata = newEvent.getEventMetadata();
                    String recurringEventId = newEvent.getRecurrence().getRecurringEventId();
                    metadata.setCalendarEventId(getRecurrentEventId(recurringEventId, newEvent.getStartDateTime()));
                }
                childEvents.add(newEvent);
            }
        } catch (ParseException e) {
            throw new ServiceErrorException(e);
        }
        return childEvents;
    }

    private void includeCreatorAsAttendee(CreateActivityRQ createActivityRQ) {
        UserProfileDto profileDto = userServiceFeignClient.findMyUserProfile().getBody();
        if (profileDto != null) {
            boolean isAlreadyIncluded = createActivityRQ.getEventDto().getAttendees()
                    .stream().anyMatch(eventAttendeeDto -> eventAttendeeDto.getUserEmailAddress().equals(profileDto.getEmail()));
            if (!isAlreadyIncluded) {
                createActivityRQ.getEventDto().getAttendees()
                        .add(EventAttendeeDto.builder()
                                .userId(profileDto.getUserId())
                                .displayName(profileDto.getFirstName() + " " + profileDto.getLastName())
                                .userEmailAddress(profileDto.getEmail())
                                .build());
            }
        }
    }

    @Override
    @Transactional
    public EventDto updateEvent(String eventId, EventDto eventDTO) {
        EventEntity event = eventRepository.findByEventIdAndCreatedBy(eventId, getUsername());
        if (event == null) {
            throw new EntityNotFoundException("Event is not found");
        }

        if (eventDTO.getAttendees().size() != event.getAttendees().size()) {
            EventEntity finalEvent = event;
            List<EventAttendeeEntity> newAttendee = eventDTO.getAttendees().stream()
                    .filter(eventAttendeeDto -> finalEvent.getAttendees().stream()
                            .noneMatch(eventAttendeeEntity -> eventAttendeeEntity.getUserEmailAddress()
                                    .equals(eventAttendeeDto.getUserEmailAddress())))
                    .map(eventAttendeeDto -> modelMapper.map(eventAttendeeDto, EventAttendeeEntity.class))
                    .toList();
            List<EventAttendeeEntity> removedAttendee = finalEvent.getAttendees().stream()
                    .filter(eventAttendeeEntity -> eventDTO.getAttendees().stream()
                            .noneMatch(eventAttendeeDto -> eventAttendeeEntity.getUserEmailAddress()
                                    .equals(eventAttendeeDto.getUserEmailAddress()))).toList();

            event.getAttendees().removeAll(removedAttendee);
            event.getAttendees().addAll(newAttendee);
        }

        event = event.toBuilder()
                .title(eventDTO.getTitle())
                .location(eventDTO.getLocation())
                .description(eventDTO.getDescription())
                .startDateTime(eventDTO.getStartDateTime())
                .startTimeZone(eventDTO.getStartTimeZone())
                .endDateTime(eventDTO.getEndDateTime())
                .endTimeZone(eventDTO.getEndTimeZone())
                .isCreateConference(eventDTO.isCreateConference())
                .duration(getDuration(eventDTO.getStartDateTime(), eventDTO.getEndDateTime()).toMinutes())
                .active(true)
                .build();
        event = eventRepository.save(event);
        try {
            if (event.getEventMetadata() != null) {
                updateCalendarEvent(event, eventDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modelMapper.map(event, EventDto.class);
    }

    @Override
    @Transactional
    public void cancelEvent(String eventId) {
        EventEntity event = eventRepository.findByEventIdAndCreatedBy(eventId, getUsername());
        if (event == null) {
            throw new EntityNotFoundException("Event is not found");
        }
        if (event.getRecurrence() != null) {
            cancelRecurrentEvent(eventId, event.getRecurrence().getRecurringEventId());
            return;
        }
        try {
            if (event.getEventMetadata() != null) {
                ServiceType serviceType = ServiceType.valueOf(event.getEventMetadata().getCalendarProvider());
                deleteCalendarEvent(event.getEventMetadata().getCalendarEventId(), serviceType);
            }
        } catch (ServiceErrorException e) {
            e.printStackTrace();
        }
        event.setEventMetadata(null);
        event.setActive(false);
        eventRepository.save(event);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelEvents(List<String> eventId) {
        eventId.forEach(this::cancelEvent);
    }

    @Override
    public void cancelRecurrentEvent(String eventId, String recurrenceEventId) {
        EventEntity event = eventRepository.findByEventIdAndCreatedBy(eventId, getUsername());
        if (event == null) {
            throw new EntityNotFoundException("Event is not found");
        }
        try {
            if (event.getEventMetadata() != null) {
                ServiceType serviceType = ServiceType.valueOf(event.getEventMetadata().getCalendarProvider());
                deleteCalendarRecurrentEvent(event.getEventMetadata().getCalendarEventId(), recurrenceEventId, serviceType);
            }
        } catch (ServiceErrorException e) {
            e.printStackTrace();
        }
        event.setEventMetadata(null);
        event.setActive(false);
        eventRepository.save(event);
    }

    @Override
    public void cancelAllRecurrentEvent(String recurrenceEventId) {
        List<EventEntity> events = eventRepository.findAllByCreatedByAndRecurrence_RecurringEventId(getUsername(), recurrenceEventId);
        if (events == null || events.isEmpty()) {
            throw new ServiceErrorException("Events are not found");
        }

        EventEntity event = events.get(0);
        if (event.getEventMetadata() != null) {
            ServiceType serviceType = ServiceType.valueOf(event.getEventMetadata().getCalendarProvider());
            deleteCalendarRecurrentEvent(null, recurrenceEventId, serviceType);
        }

        for (EventEntity eventEntity : events) {
            eventEntity.setEventMetadata(null);
            eventEntity.setActive(false);
        }
        eventRepository.saveAll(events);
    }

    @Override
    @Transactional
    public void deleteEvent(String eventId) {
        EventEntity event = eventRepository.findByEventIdAndCreatedBy(eventId, getUsername());
        if (event == null) {
            throw new EntityNotFoundException("Event is not found");
        }
        eventRepository.delete(event);
    }

    @Override
    @Transactional
    public void deleteEvents(List<String> eventIds) {
        List<EventEntity> events = eventRepository.findAllByEventIdInAndCreatedBy(eventIds, getUsername());
        eventRepository.deleteAll(events);
    }

    @Override
    public List<EventDto> getAllEvents() {
        List<EventEntity> eventEntities = eventRepository.findAll();
        return eventEntities.stream()
                .map(eventEntity -> modelMapper.map(eventEntity, EventDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDto> getAllEvents(String createdFor) {
        List<EventEntity> eventEntities = eventRepository.findAllByCreatedFor(createdFor);
        return eventEntities.stream()
                .map(eventEntity -> modelMapper.map(eventEntity, EventDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public PageDto<EventDto> findEvents(SearchQuery searchQuery) {
        Specification<EventEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        Page<EventEntity> page = eventRepository.findAll(specification, pageable);
        return GeneralSpecification.getPageResponse(page, eventEntity -> modelMapper.map(eventEntity, EventDto.class));
    }

    public EventDto scheduleCalendarEvent(String eventId, CreateActivityRQ rq) {
        EventEntity event = eventRepository.findByEventIdAndCreatedBy(eventId, getUsername());
        if (event == null) {
            throw new EntityNotFoundException("Event with such id is not found");
        }
        EventMetadataEntity metadataEntity = null;
        try {
            metadataEntity = createCalendarEvent(rq);
        } catch (Exception e) {
            if (metadataEntity != null) {
                ServiceType serviceType = ServiceType.valueOf(metadataEntity.getCalendarProvider());
                deleteCalendarEvent(metadataEntity.getCalendarEventId(), serviceType);
            }
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }

        if (metadataEntity.getRecurringEventId() != null && event.getRecurrence() != null) {
            event.getRecurrence().setRecurringEventId(metadataEntity.getRecurringEventId());
        }
        event.setEventMetadata(metadataEntity);
        event = eventRepository.save(event);

        if (event.getRecurrence() != null) {
            List<EventEntity> events = eventRepository.findAllByCreatedByAndRecurrence_RecurringEventId(getUsername(), event.getRecurrence().getRecurringEventId());
            for (EventEntity eventEntity : events) {
                eventEntity.setEventMetadata(metadataEntity);
                if (metadataEntity.getRecurringEventId() != null && eventEntity.getRecurrence() != null) {
                    eventEntity.getRecurrence().setRecurringEventId(metadataEntity.getRecurringEventId());
                }
            }
            eventRepository.saveAll(events);
        }

        return modelMapper.map(event, EventDto.class);
    }

    private EventMetadataEntity createCalendarEvent(CreateActivityRQ createActivityRQ) {
        EventMetadataEntity metadataEntity = null;
        ServiceType serviceType = ServiceType.valueOf(createActivityRQ.getCalendarProvider());
        if (serviceType.equals(ServiceType.GOOGLE_CALENDAR)) {
            GoogleCalendarEventDto eventDto = calendarIntegrationService.createGoogleCalendarEvent(createActivityRQ);
            String calendarEventId = eventDto.getCalendarEventId();
            if (createActivityRQ.getEventDto() != null) {
                calendarEventId = getRecurrentEventId(eventDto.getCalendarEventId(), createActivityRQ.getEventDto().getStartDateTime());
            }
            metadataEntity = EventMetadataEntity.builder()
                    .calendarProvider(createActivityRQ.getCalendarProvider())
                    .conferenceProvider(createActivityRQ.getConferenceProvider())
                    .calendarEventId(calendarEventId)
                    .calendarEventUID(eventDto.getCalendarEventUID())
                    .calendarEventLink(eventDto.getEventLink())
                    .conferenceLink(eventDto.getConferenceLink())
                    .recurringEventId(eventDto.getRecurringEventId())
                    .build();
        }
        return metadataEntity;
    }

    public void updateCalendarEvent(EventEntity event, EventDto eventDto) {
        if (ServiceType.GOOGLE_CALENDAR.toString().equals(event.getEventMetadata().getCalendarProvider())) {
            calendarIntegrationService.updateGoogleCalendarEvent(event.getEventMetadata().getCalendarEventId(), eventDto);
        }
    }

    public void deleteCalendarEvent(String eventId, ServiceType serviceType) {
        if (ServiceType.GOOGLE_CALENDAR.equals(serviceType)) {
            calendarIntegrationService.deleteGoogleCalendarEvent(eventId);
        }
    }

    public void deleteCalendarRecurrentEvent(String eventId, String recurrenceEventId, ServiceType serviceType) {
        if (ServiceType.GOOGLE_CALENDAR.equals(serviceType)) {
            calendarIntegrationService.deleteGoogleCalendarRecurrentEvent(eventId, recurrenceEventId);
        }
    }
}
