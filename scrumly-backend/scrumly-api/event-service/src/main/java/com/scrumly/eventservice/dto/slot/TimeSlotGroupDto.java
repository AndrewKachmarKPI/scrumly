package com.scrumly.eventservice.dto.slot;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TimeSlotGroupDto {
    private LocalDate date;
    private List<TimeSlotDto> timeSlotDto;
}
