package com.scrumly.api.backlog;

import com.scrumly.dto.backlog.BacklogDto;
import com.scrumly.dto.user.UserProfileDto;
import com.scrumly.service.backlog.BacklogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/backlogs")
@RequiredArgsConstructor
public class BacklogController {
    private final BacklogService backlogService;

    @GetMapping("/{teamId}/all")
    public ResponseEntity<List<BacklogDto>> getTeamBacklogs(@PathVariable("teamId") String teamId) {
        return ResponseEntity.ok(backlogService.getTeamBacklogs(teamId));
    }

    @PostMapping("/{teamId}/default")
    public ResponseEntity<BacklogDto> createBacklogDefault(@PathVariable("teamId") String teamId) {
        return ResponseEntity.ok(backlogService.createBacklogDefault(teamId));
    }

    @PostMapping("/{teamId}")
    public ResponseEntity<BacklogDto> createBacklog(@PathVariable("teamId") String teamId,
                                                    @RequestBody BacklogDto backlogDto) {
        return ResponseEntity.ok(backlogService.createBacklog(teamId, backlogDto));
    }

    @GetMapping("/{teamId}/exists")
    public ResponseEntity<Boolean> hasBacklog(@PathVariable("teamId") String teamId) {
        return ResponseEntity.ok(backlogService.hasBacklog(teamId));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<BacklogDto> getBacklogByTeamId(@PathVariable("teamId") String teamId) {
        return ResponseEntity.ok(backlogService.getBacklogById(teamId));
    }

    @GetMapping("/{backlogId}/assignee")
    public ResponseEntity<List<UserProfileDto>> getAllAssignee(@PathVariable("backlogId") String backlogId) {
        return ResponseEntity.ok(backlogService.getAllAssignee(backlogId));
    }
}
