package com.scrumly.api;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.dto.events.SyncBlockOption;
import com.scrumly.service.ActivityRoomService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.scrumly.mappers.BusinessMapper.serializeMeetingRoom;

@RestController
@RequestMapping("/api/activities/rooms")
@RequiredArgsConstructor
public class ActivityRoomController {
    private final ActivityRoomService activityRoomService;

    @PostMapping("/{activityId}")
    public ResponseEntity<String> createActivityRoom(@PathVariable("activityId") @NotBlank String activityId) {
        return ResponseEntity.ok(serializeMeetingRoom(activityRoomService.createRoom(activityId)));
    }

    @PostMapping("/{activityId}/finish")
    public ResponseEntity<Void> onFinishActivity(@PathVariable("activityId") @NotBlank String activityId) {
        activityRoomService.onFinishActivity(activityId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<String> getActivityRoom(@PathVariable("activityId") @NotBlank String activityId) {
        return ResponseEntity.ok(serializeMeetingRoom(activityRoomService.getActivityRoom(activityId)));
    }

    @GetMapping("/{activityId}/exists")
    public ResponseEntity<Boolean> isActivityRoomCreated(@PathVariable("activityId") @NotBlank String activityId) {
        return ResponseEntity.ok(activityRoomService.isActivityRoomCreated(activityId));
    }

    @PostMapping("/{activityId}/join")
    public ResponseEntity<Void> joinRoom(@PathVariable("activityId") @NotBlank String activityId) {
        activityRoomService.joinRoom(activityId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{activityId}/exit")
    public ResponseEntity<Void> exitRoom(@PathVariable("activityId") @NotBlank String activityId) {
        activityRoomService.exitRoom(activityId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{activityId}/sync-block")
    public ResponseEntity<List<SyncBlockOption>> getSyncBlockOptions(@PathVariable("activityId") @NotBlank String activityId) {
        return ResponseEntity.ok(activityRoomService.getSyncBlockOptions(activityId));
    }


    @PostMapping("/{activityId}/testExport")
    public ResponseEntity<ActivityRoom> test(@PathVariable("activityId") @NotBlank String activityId) {
        activityRoomService.test(activityId);
        return ResponseEntity.ok().build();
    }
}
