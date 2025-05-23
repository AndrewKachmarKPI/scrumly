package com.scrumly.api;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.dto.events.ActivitySummaryNotes;
import com.scrumly.dto.issues.IssueShortInfo;
import com.scrumly.service.ActivityRoomActionService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.scrumly.utils.SecurityUtils.getUsername;


@RestController
@RequestMapping("/api/activities/rooms/actions")
@RequiredArgsConstructor
public class ActivityRoomActionsController {
    private final ActivityRoomActionService roomActionService;

    @PostMapping("/{activityId}/block/{blockId}/onSelectEstimateIssues")
    public ResponseEntity<Void> onSelectIssues(@PathVariable("activityId") @NotBlank String activityId,
                                               @PathVariable("blockId") @NotBlank String blockId,
                                               @RequestBody List<IssueShortInfo> issues) {
        roomActionService.onSelectEstimateIssues(getUsername(), activityId, blockId, issues);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{activityId}/block/{blockId}/onDeleteEstimateIssues")
    public ResponseEntity<Void> onDeleteIssues(@PathVariable("activityId") @NotBlank String activityId,
                                               @PathVariable("blockId") @NotBlank String blockId,
                                               @RequestBody List<String> issues) {
        roomActionService.onDeleteEstimateIssues(getUsername(), activityId, blockId, issues);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{activityId}/block/{blockId}/onUpdateEstimateIssues")
    public ResponseEntity<Void> onUpdateIssues(@PathVariable("activityId") @NotBlank String activityId,
                                               @PathVariable("blockId") @NotBlank String blockId,
                                               @RequestBody List<String> issues) {
        roomActionService.onUpdateEstimateIssues(getUsername(), activityId, blockId, issues);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{activityId}/block/{blockId}/onSelectBoardBacklogIssues")
    public ResponseEntity<Void> onSelectBoardBacklogIssues(@PathVariable("activityId") @NotBlank String activityId,
                                                           @PathVariable("blockId") @NotBlank String blockId,
                                                           @RequestBody List<IssueShortInfo> issues) {
        roomActionService.onSelectBoardBacklogIssues(getUsername(), activityId, blockId, issues);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{activityId}/block/{blockId}/onDeleteBoardBacklogIssues")
    public ResponseEntity<Void> onDeleteBoardBacklogIssues(@PathVariable("activityId") @NotBlank String activityId,
                                                           @PathVariable("blockId") @NotBlank String blockId,
                                                           @RequestBody List<String> issues) {
        roomActionService.onDeleteBoardBacklogIssues(getUsername(), activityId, blockId, issues);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{activityId}/onGenerateNotes")
    public ResponseEntity<ActivitySummaryNotes> onGenerateActivitySummary(@PathVariable("activityId") @NotBlank String activityId) {
        return ResponseEntity.ok(roomActionService.onGenerateActivitySummary(getUsername(), activityId));
    }

    @PostMapping("/{activityId}/onSetMeetingNotes")
    public ResponseEntity<ActivityRoom.ActivityRoomNotesMetadata> onSetMeetingNotes(@PathVariable("activityId") @NotBlank String activityId,
                                                                                    @RequestBody String notes) {
        return ResponseEntity.ok(roomActionService.onSetMeetingNotes(getUsername(), activityId, notes));
    }

    @PostMapping("/{activityId}/block/{blockId}/onGenerateColumnReflection")
    public ResponseEntity<ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard> onGenerateColumnReflection(@PathVariable("activityId") @NotBlank String activityId,
                                                                                                                                              @PathVariable("blockId") @NotBlank String blockId,
                                                                                                                                              @RequestParam(value = "prompt", required = false) String prompt,
                                                                                                                                              @RequestBody ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnMetadata reflectColumnMetadata) {
        return ResponseEntity.ok(roomActionService.onGenerateColumnReflection(getUsername(), activityId, blockId, prompt, reflectColumnMetadata));
    }

}
