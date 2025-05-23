package com.scrumly.eventservice.dto.slot;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TimeSlotDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String label;
    private Boolean isOccupied;
    private List<TimeSlotConflictDto> conflicts;
}
