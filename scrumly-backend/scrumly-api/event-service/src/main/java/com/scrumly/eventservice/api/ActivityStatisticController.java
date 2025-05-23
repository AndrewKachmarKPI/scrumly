package com.scrumly.eventservice.api;

import com.scrumly.eventservice.dto.activity.ActivityDto;
import com.scrumly.eventservice.dto.requests.events.CreateActivityRQ;
import com.scrumly.eventservice.dto.requests.events.ScheduleActivityCalendarEventRQ;
import com.scrumly.eventservice.dto.requests.events.StartActivityRQ;
import com.scrumly.eventservice.dto.requests.slot.FindTimeSlotRQ;
import com.scrumly.eventservice.dto.slot.TimeSlotGroupDto;
import com.scrumly.eventservice.dto.statistic.ActivityUserStatistic;
import com.scrumly.eventservice.enums.ActivityStatus;
import com.scrumly.eventservice.services.ActivityService;
import com.scrumly.eventservice.services.ActivityStatisticService;
import com.scrumly.eventservice.services.TimeSlotService;
import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.scrumly.eventservice.utils.SecurityUtils.getUsername;

@RestController
@RequestMapping("/api/activities/statistic")
@RequiredArgsConstructor
public class ActivityStatisticController {
    private final ActivityStatisticService activityStatisticService;


    @GetMapping("/{userId}/user")
    public ResponseEntity<ActivityUserStatistic> getActivityUserStatistic(@PathVariable("userId") @NotNull String userId,
                                                                          @RequestParam(value = "startDate", required = false) LocalDate startDate,
                                                                          @RequestParam(value = "endDate", required = false) LocalDate end) {
        return ResponseEntity.ok(activityStatisticService.getActivityUserStatistic(userId, startDate, end));
    }

    @GetMapping("/me")
    public ResponseEntity<ActivityUserStatistic> getActivityUserStatistic(@RequestParam(value = "startDate", required = false) LocalDate startDate,
                                                                          @RequestParam(value = "endDate", required = false) LocalDate end) {
        return ResponseEntity.ok(activityStatisticService.getActivityUserStatistic(getUsername(), startDate, end));
    }
}
