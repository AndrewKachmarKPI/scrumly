package com.scrumly.eventservice.dto.requests.events;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleActivityCalendarEventRQ {
    @NotNull
    private String activityId;
    @NotNull
    private String calendarProvider;
    @NotNull
    private String conferenceProvider;
}
