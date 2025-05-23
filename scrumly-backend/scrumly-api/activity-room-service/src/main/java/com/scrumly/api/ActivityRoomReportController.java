package com.scrumly.api;

import com.scrumly.service.ActivityRoomReportService;
import com.scrumly.service.ActivityRoomService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.scrumly.mappers.BusinessMapper.serializeMeetingRoomReport;

@RestController
@RequestMapping("/api/activities/rooms/reports")
@RequiredArgsConstructor
public class ActivityRoomReportController {
    private final ActivityRoomReportService activityRoomReportService;

    @GetMapping("/{activityId}")
    public ResponseEntity<String> getActivityRoomReport(@PathVariable("activityId") @NotBlank String activityId) {
        return ResponseEntity.ok(serializeMeetingRoomReport(activityRoomReportService.getActivityRoomReport(activityId)));
    }

    private final ActivityRoomService roomService;

    @GetMapping("/{activityId}/test")
    public ResponseEntity<String> test(@PathVariable("activityId") @NotBlank String activityId) {
        return ResponseEntity.ok(serializeMeetingRoomReport(activityRoomReportService.test(roomService.getActivityRoom(activityId))));
    }
}
