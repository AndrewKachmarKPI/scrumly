package com.scrumly.eventservice.dto.slot;

import com.scrumly.eventservice.dto.user.UserProfileDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TimeSlotConflictDto {
    private String eventId;
    private String title;
    private List<UserProfileDto> conflictingUsers;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
