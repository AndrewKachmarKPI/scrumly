package com.scrumly.eventservice.dto.statistic;

import com.scrumly.eventservice.dto.activity.ActivityDto;
import com.scrumly.eventservice.enums.ActivityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ActivityUserStatistic {
    private Integer totalEventsThisWeek;
    private Double averageEventDuration;

    private Integer totalPastEvents;
    private Integer totalUpcomingEvents;

    private EventStatusBreakdown eventStatusBreakdown;
    private EventDurationBreakdown eventDurationBreakdown;
    private AttendanceStats attendanceStats;
    private ActivityUserStatistic.WeeklyMeetingLoad weeklyMeetingLoadStats;

    @Getter
    @Setter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventStatusBreakdown {
        private List<EventStatusRecord> statusRecords;

        @Getter
        @Setter
        @Builder(toBuilder = true)
        @AllArgsConstructor
        @NoArgsConstructor
        public static class EventStatusRecord {
            private Integer total;
            private ActivityStatus status;
        }
    }

    @Getter
    @Setter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventDurationBreakdown {
        private Double longestEventDuration;
        private Double shortestEventDuration;
    }

    @Getter
    @Setter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AttendanceStats {
        private Integer totalAttendees;
        private Double averageAttendeesPerEvent;
    }

    @Getter
    @Setter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeeklyMeetingLoad {
        private String title;
        private List<WeeklyLoadRecord> weeklyLoadRecords;

        @Getter
        @Setter
        @Builder(toBuilder = true)
        @AllArgsConstructor
        @NoArgsConstructor
        public static class WeeklyLoadRecord {
            private String day;
            private Integer total;
            private List<ActivityDto> activities;
        }
    }
}
