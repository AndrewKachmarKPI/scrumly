package com.scrumly.eventservice.services.impl;

import com.scrumly.eventservice.domain.activity.ActivityEntity;
import com.scrumly.eventservice.domain.events.EventEntity;
import com.scrumly.eventservice.dto.activity.ActivityDto;
import com.scrumly.eventservice.dto.events.EventDto;
import com.scrumly.eventservice.dto.statistic.ActivityUserStatistic;
import com.scrumly.eventservice.enums.ActivityStatus;
import com.scrumly.eventservice.mapper.BusinessMapper;
import com.scrumly.eventservice.repository.ActivityEntityRepository;
import com.scrumly.eventservice.repository.EventRepository;
import com.scrumly.eventservice.services.ActivityStatisticService;
import com.scrumly.specification.CompareOption;
import com.scrumly.specification.GeneralSpecification;
import com.scrumly.specification.SearchFilter;
import com.scrumly.specification.SearchOperators;
import com.scrumly.specification.SearchQuery;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityStatisticServiceImpl implements ActivityStatisticService {
    private final ActivityEntityRepository activityRepository;
    private final BusinessMapper businessMapper;

    @Override
    public ActivityUserStatistic getActivityUserStatistic(String username, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = getStartOfWeek();
        LocalDateTime endDateTime = getEndOfWeek();
        if (startDate != null && endDate != null) {
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atTime(LocalTime.MAX);
        }
        return collectActivityUserStatistic(username, startDateTime, endDateTime);
    }

    private ActivityUserStatistic collectActivityUserStatistic(String username,
                                                               LocalDateTime startDate,
                                                               LocalDateTime endDate) {
        // Get activities within the specified date range
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.appendSearchFilter(SearchFilter.builder()
                                               .property("scheduledEvent.startDateTime")
                                               .value(startDate.toString())
                                               .compareOption(CompareOption.AND)
                                               .operator(SearchOperators.GREATER_THAN_OR_EQUAL_TO.name())
                                               .build());
        searchQuery.appendSearchFilter(SearchFilter.builder()
                                               .property("scheduledEvent.endDateTime")
                                               .value(endDate.toString())
                                               .compareOption(CompareOption.AND)
                                               .operator(SearchOperators.LESS_THAN_OR_EQUAL_TO.name())
                                               .build());
        searchQuery.appendSearchFilter(SearchFilter.builder()
                                               .property("scheduledEvent.attendees.userId")
                                               .value(username)
                                               .compareOption(CompareOption.AND)
                                               .operator(SearchOperators.IN.name())
                                               .build());

        // Fetch activity data
        List<ActivityDto> activities = findActivities(searchQuery);

        // Calculate the total number of activities (events) this week
        Integer totalEventsThisWeek = activities.size();

        // Calculate the average event duration for activities
        Double averageEventDuration = calculateAverageEventDuration(activities);

        // Round the averageEventDuration to 2 decimal places
        averageEventDuration = round(averageEventDuration, 2);

        // Calculate total past and upcoming events
        Integer totalPastEvents = (int) activities.stream().filter(this::isPastEvent).count();
        Integer totalUpcomingEvents = totalEventsThisWeek - totalPastEvents;

        // Collect event status breakdown
        ActivityUserStatistic.EventStatusBreakdown eventStatusBreakdown = collectEventStatusBreakdown(activities);

        // Collect event duration breakdown (longest and shortest)
        ActivityUserStatistic.EventDurationBreakdown eventDurationBreakdown = collectEventDurationBreakdown(activities);

        // Collect attendance stats
        ActivityUserStatistic.AttendanceStats attendanceStats = collectAttendanceStats(activities);

        ActivityUserStatistic.WeeklyMeetingLoad weeklyMeetingLoadStats = collectWeeklyMeetingLoad(activities, startDate, endDate);


        return ActivityUserStatistic.builder()
                .totalEventsThisWeek(totalEventsThisWeek)
                .averageEventDuration(averageEventDuration)
                .totalPastEvents(totalPastEvents)
                .totalUpcomingEvents(totalUpcomingEvents)
                .eventStatusBreakdown(eventStatusBreakdown)
                .eventDurationBreakdown(eventDurationBreakdown)
                .attendanceStats(attendanceStats)
                .weeklyMeetingLoadStats(weeklyMeetingLoadStats)
                .build();
    }

    // Calculate the average event duration for a list of activities
    private Double calculateAverageEventDuration(List<ActivityDto> activities) {
        return activities.stream()
                .mapToDouble(activity -> activity.getScheduledEvent().getDuration())
                .average()
                .orElse(0.0);
    }

    // Round a number to the specified number of decimal places
    private Double round(Double value, int places) {
        if (value == null) {
            return 0.0;
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // Check if an event is in the past
    private boolean isPastEvent(ActivityDto activity) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime originalEnd = ZonedDateTime.parse(activity.getScheduledEvent().getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME);
        return originalEnd.isBefore(now);
    }

    // Collect event status breakdown for a list of activities
    private ActivityUserStatistic.EventStatusBreakdown collectEventStatusBreakdown(List<ActivityDto> activities) {
        Map<ActivityStatus, Long> statusCount = activities.stream()
                .map(ActivityDto::getStatus)  // Get the status from the activity itself
                .collect(Collectors.groupingBy(status -> status, Collectors.counting()));

        List<ActivityUserStatistic.EventStatusBreakdown.EventStatusRecord> statusRecords = statusCount.entrySet().stream()
                .map(entry -> ActivityUserStatistic.EventStatusBreakdown.EventStatusRecord.builder()
                        .status(entry.getKey())
                        .total(entry.getValue().intValue())
                        .build())
                .sorted(Comparator.comparing(ActivityUserStatistic.EventStatusBreakdown.EventStatusRecord::getStatus))
                .collect(Collectors.toList());

        return ActivityUserStatistic.EventStatusBreakdown.builder()
                .statusRecords(statusRecords)
                .build();
    }

    // Collect event duration breakdown (longest and shortest) for a list of activities
    private ActivityUserStatistic.EventDurationBreakdown collectEventDurationBreakdown(List<ActivityDto> activities) {
        OptionalDouble longestEvent = activities.stream()
                .mapToDouble(activity -> activity.getScheduledEvent().getDuration()).max();
        OptionalDouble shortestEvent = activities.stream()
                .mapToDouble(activity -> activity.getScheduledEvent().getDuration()).min();
        return ActivityUserStatistic.EventDurationBreakdown.builder()
                .longestEventDuration(longestEvent.orElse(0.0))
                .shortestEventDuration(shortestEvent.orElse(0.0))
                .build();
    }

    private ActivityUserStatistic.WeeklyMeetingLoad collectWeeklyMeetingLoad(List<ActivityDto> activities,
                                                                             LocalDateTime startDate,
                                                                             LocalDateTime endDate) {
        List<ActivityUserStatistic.WeeklyMeetingLoad.WeeklyLoadRecord> weeklyLoadRecords = new ArrayList<>();
        LocalDate anchorDate = startDate.toLocalDate().with(DayOfWeek.MONDAY);

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            LocalDate currentDate = anchorDate.with(dayOfWeek);
            List<ActivityDto> currentActivities = activities.stream()
                    .filter(activityDto -> {
                        ZonedDateTime originalStart = ZonedDateTime.parse(activityDto.getScheduledEvent().getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME);
                        return originalStart.getDayOfWeek().equals(dayOfWeek);
                    })
                    .toList();
            String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH));
            String title = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + ", " + formattedDate;
            weeklyLoadRecords.add(ActivityUserStatistic.WeeklyMeetingLoad.WeeklyLoadRecord.builder()
                                          .day(title)
                                          .total(currentActivities.size())
                                          .activities(currentActivities)
                                          .build());
        }

        return ActivityUserStatistic.WeeklyMeetingLoad.builder()
                .weeklyLoadRecords(weeklyLoadRecords)
                .build();
    }

    // Collect attendance stats (total attendees and average attendees per event)
    private ActivityUserStatistic.AttendanceStats collectAttendanceStats(List<ActivityDto> activities) {
        int totalAttendees = activities.stream()
                .mapToInt(activity -> activity.getScheduledEvent().getAttendees().size())  // Get attendees from the scheduled event
                .sum();

        double averageAttendeesPerEvent = activities.isEmpty() ? 0 : (double) totalAttendees / activities.size();

        return ActivityUserStatistic.AttendanceStats.builder()
                .totalAttendees(totalAttendees)
                .averageAttendeesPerEvent(round(averageAttendeesPerEvent, 2))  // Round average attendees to 2 decimal places
                .build();
    }

    // Get the start of the week
    public static LocalDateTime getStartOfWeek() {
        LocalDateTime now = LocalDateTime.now();
        return now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
    }

    // Get the end of the week
    public static LocalDateTime getEndOfWeek() {
        LocalDateTime now = LocalDateTime.now();
        return now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    private List<ActivityDto> findActivities(SearchQuery searchQuery) {
        Specification<ActivityEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        Page<ActivityEntity> page = activityRepository.findAll(specification, pageable);
        return GeneralSpecification.getPageResponse(page, businessMapper::activityToDtoWithoutActivity)
                .getData();
    }
}
