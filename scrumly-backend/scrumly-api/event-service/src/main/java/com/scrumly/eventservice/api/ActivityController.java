package com.scrumly.eventservice.api;

import com.scrumly.eventservice.dto.activity.ActivityDto;
import com.scrumly.eventservice.dto.requests.events.CreateActivityRQ;
import com.scrumly.eventservice.dto.requests.events.ScheduleActivityCalendarEventRQ;
import com.scrumly.eventservice.dto.requests.events.StartActivityRQ;
import com.scrumly.eventservice.dto.requests.slot.FindTimeSlotRQ;
import com.scrumly.eventservice.dto.slot.TimeSlotGroupDto;
import com.scrumly.eventservice.enums.ActivityStatus;
import com.scrumly.eventservice.services.ActivityService;
import com.scrumly.eventservice.services.TimeSlotService;
import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;
    private final TimeSlotService timeSlotService;

    @PostMapping("/start")
    public ResponseEntity<ActivityDto> startActivity(@RequestBody StartActivityRQ startActivityRQ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityService.startActivity(startActivityRQ));
    }

    @PostMapping
    public ResponseEntity<List<ActivityDto>> scheduleActivity(@Valid @RequestBody CreateActivityRQ createActivityRQ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityService.scheduleActivity(createActivityRQ));
    }

    @PostMapping("/{activityId}/reschedule")
    public ResponseEntity<ActivityDto> rescheduleActivity(@PathVariable("activityId") @NotNull String activityId,
                                                          @Valid @RequestBody CreateActivityRQ createActivityRQ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityService.rescheduleActivity(activityId, createActivityRQ));
    }

    @PostMapping("/{activityId}/reschedule/recurrent")
    public ResponseEntity<List<ActivityDto>> rescheduleRecurrentActivity(@PathVariable("activityId") @NotNull String activityId,
                                                                         @Valid @RequestBody CreateActivityRQ createActivityRQ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityService.rescheduleRecurrentActivity(activityId, createActivityRQ));
    }

    @PostMapping("/schedule/event")
    public ResponseEntity<ActivityDto> scheduleActivityCalendarEvent(@Valid @RequestBody ScheduleActivityCalendarEventRQ rq) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityService.scheduleActivityCalendarEvent(rq));
    }


    @PostMapping("/all")
    public ResponseEntity<PageDto<ActivityDto>> findActivities(@Valid @RequestBody SearchQuery searchQuery) {
        return ResponseEntity.ok(activityService.findActivities(searchQuery));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityDto> getActivityById(@PathVariable String activityId) {
        return ResponseEntity.ok(activityService.getActivityById(activityId));
    }

    @PostMapping("/{activityId}/ref")
    public ResponseEntity<Void> saveRoomReference(@PathVariable String activityId, @RequestParam String roomId) {
        activityService.saveRoomReference(activityId, roomId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(@PathVariable @NotNull String activityId) {
        activityService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{activityId}/cancel")
    public ResponseEntity<Void> cancelActivity(@PathVariable @NotNull String activityId) {
        activityService.cancelActivity(activityId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{recurrentEventId}/cancel/recurrent")
    public ResponseEntity<Void> cancelAllRecurrentActivity(@PathVariable @NotNull String recurrentEventId) {
        activityService.cancelAllRecurrentActivity(recurrentEventId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{recurrentEventId}/recurrent")
    public ResponseEntity<Void> deleteAllRecurrentActivity(@PathVariable @NotNull String recurrentEventId) {
        activityService.deleteAllRecurrentActivity(recurrentEventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/slot/availability")
    public ResponseEntity<List<TimeSlotGroupDto>> findTimeSlotRQ(@RequestBody FindTimeSlotRQ findTimeSlotRQ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(timeSlotService.findAvailableTimeSlots(findTimeSlotRQ));
    }

    @PutMapping("/{activityId}/status")
    public ResponseEntity<ActivityDto> changeActivityStatus(@PathVariable("activityId") @NotNull String activityId,
                                                            @Valid @RequestBody ActivityStatus activityStatus) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityService.changeActivityStatus(activityId, activityStatus));
    }

    @GetMapping("/team/{teamId}/template/{templateId}")
    public ResponseEntity<List<ActivityDto>> getRecentTemplateActivities(@PathVariable @NotNull String teamId,
                                                                         @PathVariable @NotNull String templateId) {
        return ResponseEntity.ok(activityService.getRecentTemplateActivities(teamId, templateId));
    }
}
