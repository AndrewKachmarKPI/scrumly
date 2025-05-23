package com.scrumly.eventservice.dto.requests.slot;

import com.scrumly.eventservice.dto.user.UserProfileDto;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FindTimeSlotRQ {
    private Date startDate;
    private Date endDate;
    private Date minDayTime;
    private Date maxDayTime;
    private String userTimeZone;
    private Integer meetingDuration;
    private List<UserProfileDto> invitedUsers;

    public LocalDate getEndDate() {
        return startDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public LocalDate getStartDate() {
        return startDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public LocalTime getMinDayTime() {
        return minDayTime.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
    }

    public LocalTime getMaxDayTime() {
        return maxDayTime.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
    }

    public LocalTime getMinDayTime(ZoneId zoneId) {
        return minDayTime.toInstant()
                .atZone(zoneId)
                .toLocalTime();
    }

    public LocalTime getMaxDayTime(ZoneId zoneId) {
        return maxDayTime.toInstant()
                .atZone(zoneId)
                .toLocalTime();
    }
}
