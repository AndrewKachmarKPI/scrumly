package com.scrumly.eventservice.services.impl;

import com.scrumly.eventservice.domain.events.EventAttendeeEntity;
import com.scrumly.eventservice.domain.events.EventEntity;
import com.scrumly.eventservice.dto.events.EventDto;
import com.scrumly.eventservice.dto.requests.slot.FindTimeSlotRQ;
import com.scrumly.eventservice.dto.slot.TimeSlotConflictDto;
import com.scrumly.eventservice.dto.slot.TimeSlotDto;
import com.scrumly.eventservice.dto.slot.TimeSlotGroupDto;
import com.scrumly.eventservice.dto.user.UserProfileDto;
import com.scrumly.eventservice.repository.EventRepository;
import com.scrumly.eventservice.services.TimeSlotService;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.specification.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {
    private final EventRepository eventRepository;

    @Override
    public List<TimeSlotGroupDto> findAvailableTimeSlots(FindTimeSlotRQ request) {
        List<TimeSlotGroupDto> availableTimeSlots = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();

        long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        if (daysBetween > 7) {
            throw new IllegalArgumentException("The date range cannot exceed 7 days.");
        }

        ZoneId userTimeZone = ZoneId.of(request.getUserTimeZone()); // Ensure request contains user's timezone

        while (!currentDate.isAfter(request.getEndDate())) {
            List<EventEntity> eventsForDay = findEventsByDateAndUsers(request, currentDate);
            List<TimeSlotDto> timeSlots = findAvailableSlotsWithConflicts(eventsForDay, currentDate, userTimeZone, request);
            if (!timeSlots.isEmpty()) {
                availableTimeSlots.add(new TimeSlotGroupDto(currentDate, timeSlots));
            }
            currentDate = currentDate.plusDays(1);
        }
        return availableTimeSlots;
    }

    private List<EventEntity> findEventsByDateAndUsers(FindTimeSlotRQ request, LocalDate currentDay) {
        ZonedDateTime startOfDayUTC = currentDay.atStartOfDay(ZoneId.of(request.getUserTimeZone())).withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime endOfDayUTC = currentDay.atTime(LocalTime.MAX).atZone(ZoneId.of(request.getUserTimeZone())).withZoneSameInstant(ZoneOffset.UTC);
        List<String> userIds = request.getInvitedUsers().stream().map(UserProfileDto::getUserId).toList();

        SearchQuery searchQuery = new SearchQuery();
        searchQuery.appendSearchFilter(new SearchFilter(
                "startDateTime",
                SearchOperators.GREATER_THAN_OR_EQUAL_TO,
                CompareOption.AND,
                startOfDayUTC.toString()));
        searchQuery.appendSearchFilter(new SearchFilter(
                "startDateTime",
                SearchOperators.LESS_THAN_OR_EQUAL_TO,
                CompareOption.AND,
                endOfDayUTC.toString()));
        searchQuery.appendSearchFilter(new SearchFilter(
                "attendees.userId",
                SearchOperators.IN,
                CompareOption.AND,
                userIds));
        searchQuery.setPageNumber(0);
        searchQuery.setPageSize(10000);

        Specification<EventEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        return eventRepository.findAll(specification, pageable).getContent();
    }

    private List<TimeSlotDto> findAvailableSlotsWithConflicts(List<EventEntity> events,
                                                              LocalDate date,
                                                              ZoneId userTimeZone,
                                                              FindTimeSlotRQ request) {

        List<TimeSlotDto> availableSlots = new ArrayList<>();

        // Sort events by start time (assume UTC)
        List<EventEntity> sortedEvents = events.stream()
                .sorted(Comparator.comparing(EventEntity::getStartDateTime))
                .toList();

        // Convert day start and end to user's local time
        ZonedDateTime dayStart = date.atTime(request.getMinDayTime()).atZone(userTimeZone);
        ZonedDateTime dayEnd = date.atTime(request.getMaxDayTime()).atZone(userTimeZone);
        ZonedDateTime currentStart = dayStart;

        Duration meetingDurationMinutes = Duration.ofMinutes(request.getMeetingDuration());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        ZonedDateTime lastOccupiedEnd = null;  // Track the end of the last occupied period

        for (EventEntity event : sortedEvents) {
            ZonedDateTime eventStartUTC = ZonedDateTime.parse(event.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME);
            ZonedDateTime eventEndUTC = ZonedDateTime.parse(event.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME);

            // Convert to user's timezone
            ZonedDateTime eventStart = eventStartUTC.withZoneSameInstant(userTimeZone);
            ZonedDateTime eventEnd = eventEndUTC.withZoneSameInstant(userTimeZone);

            // Add available slots before the occupied period
            while (currentStart.plus(meetingDurationMinutes).isBefore(eventStart) ||
                    currentStart.plus(meetingDurationMinutes).equals(eventStart)) {
                String label = currentStart.format(timeFormatter) + " - " +
                        currentStart.plus(meetingDurationMinutes).format(timeFormatter);
                availableSlots.add(TimeSlotDto.builder()
                        .startTime(currentStart.toLocalDateTime())
                        .endTime(currentStart.plus(meetingDurationMinutes).toLocalDateTime())
                        .label(label)
                        .isOccupied(false) // Free slot
                        .conflicts(new ArrayList<>()) // No conflicts in free slots
                        .build());
                currentStart = currentStart.plus(meetingDurationMinutes);
            }

            // Check if this event extends an existing occupied period
            if (lastOccupiedEnd != null && eventStart.isBefore(lastOccupiedEnd)) {
                // Merge overlapping events
                lastOccupiedEnd = lastOccupiedEnd.isAfter(eventEnd) ? lastOccupiedEnd : eventEnd;
            } else {
                // If it's a new occupied period, add it separately
                List<EventEntity> conflictingEvents = sortedEvents.stream()
                        .filter(e -> isOverlapping(eventStart, eventEnd, e, userTimeZone))
                        .distinct()
                        .toList();

                List<TimeSlotConflictDto> conflicts = conflictingEvents.stream()
                        .map(conflictEvent -> {
                            List<String> attendeesIds = conflictEvent.getAttendees().stream()
                                    .map(EventAttendeeEntity::getUserId)
                                    .toList();
                            return TimeSlotConflictDto.builder()
                                    .eventId(conflictEvent.getEventId())
                                    .title(conflictEvent.getTitle())
                                    .startTime(eventStart.toLocalDateTime())
                                    .endTime(eventEnd.toLocalDateTime())
                                    .conflictingUsers(request.getInvitedUsers().stream()
                                            .filter(userProfileDto -> attendeesIds.contains(userProfileDto.getUserId()))
                                            .toList())
                                    .build();
                        })
                        .toList();

                String conflictLabel = eventStart.format(timeFormatter) + " - " + eventEnd.format(timeFormatter);

                availableSlots.add(TimeSlotDto.builder()
                        .startTime(eventStart.toLocalDateTime())
                        .endTime(eventEnd.toLocalDateTime())
                        .label(conflictLabel)
                        .isOccupied(true) // Occupied slot
                        .conflicts(conflicts) // Add conflicts here
                        .build());

                lastOccupiedEnd = eventEnd;
            }

            // Move currentStart forward if it's within an occupied period
            if (eventEnd.isAfter(currentStart)) {
                currentStart = eventEnd;
            }
        }

        // Add last available slot if there's room at the end of the day
        while (currentStart.plus(meetingDurationMinutes).isBefore(dayEnd) ||
                currentStart.plus(meetingDurationMinutes).equals(dayEnd)) {
            String label = currentStart.format(timeFormatter) + " - " +
                    currentStart.plus(meetingDurationMinutes).format(timeFormatter);
            availableSlots.add(TimeSlotDto.builder()
                    .startTime(currentStart.toLocalDateTime())
                    .endTime(currentStart.plus(meetingDurationMinutes).toLocalDateTime())
                    .label(label)
                    .isOccupied(false) // Free slot
                    .conflicts(new ArrayList<>()) // No conflicts in this slot
                    .build());
            currentStart = currentStart.plus(meetingDurationMinutes);
        }

        return availableSlots;
    }


    private boolean isOverlapping(ZonedDateTime start1, ZonedDateTime end1, EventEntity event, ZoneId userTimeZone) {
        ZonedDateTime start2UTC = ZonedDateTime.parse(event.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME);
        ZonedDateTime end2UTC = ZonedDateTime.parse(event.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME);

        ZonedDateTime start2 = start2UTC.withZoneSameInstant(userTimeZone);
        ZonedDateTime end2 = end2UTC.withZoneSameInstant(userTimeZone);

        return (start1.isBefore(end2) && end1.isAfter(start2));
    }
}
