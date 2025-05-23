package com.scrumly.eventservice.services;

import com.scrumly.eventservice.dto.requests.slot.FindTimeSlotRQ;
import com.scrumly.eventservice.dto.slot.TimeSlotGroupDto;

import java.util.List;

public interface TimeSlotService {
    List<TimeSlotGroupDto> findAvailableTimeSlots(FindTimeSlotRQ request);
}
