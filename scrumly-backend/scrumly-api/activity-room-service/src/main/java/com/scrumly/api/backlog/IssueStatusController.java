package com.scrumly.api.backlog;

import com.scrumly.dto.backlog.BacklogIssueStatusesDto;
import com.scrumly.dto.backlog.IssueStatusDto;
import com.scrumly.service.backlog.IssueStatusService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/issue-statuses")
@RequiredArgsConstructor
public class IssueStatusController {
    private final IssueStatusService issueStatusService;

    // Get all issue statuses
    @GetMapping("/{backlogId}/all")
    public ResponseEntity<List<IssueStatusDto>> getAllIssueStatuses(@PathVariable("backlogId") String backlogId) {
        List<IssueStatusDto> issueStatusDtos = issueStatusService.getAllIssueStatuses(backlogId);
        return ResponseEntity.ok(issueStatusDtos);
    }

    // Get issue status by ID
    @GetMapping("/{id}")
    public ResponseEntity<IssueStatusDto> getIssueStatusById(@PathVariable Long id) {
        IssueStatusDto issueStatusDto = issueStatusService.getIssueStatusById(id);
        return ResponseEntity.ok(issueStatusDto);
    }

    // Create a new issue status
    @PostMapping
    public ResponseEntity<IssueStatusDto> createIssueStatus(@RequestBody IssueStatusDto issueStatusDto) {
        IssueStatusDto createdIssueStatus = issueStatusService.createIssueStatus(issueStatusDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIssueStatus);
    }

    // Update an existing issue status
    @PutMapping("/{id}")
    public ResponseEntity<IssueStatusDto> updateIssueStatus(@PathVariable Long id, @RequestBody IssueStatusDto issueStatusDto) {
        IssueStatusDto updatedIssueStatus = issueStatusService.updateIssueStatus(id, issueStatusDto);
        return ResponseEntity.ok(updatedIssueStatus);
    }

    // Delete an issue status by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssueStatus(@PathVariable Long id) {
        issueStatusService.deleteIssueStatus(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/team/{teamId}/all")
    public ResponseEntity< List<BacklogIssueStatusesDto>> getTeamAllIssueStatuses(@PathVariable("teamId") String teamId) {
        List<BacklogIssueStatusesDto> issueStatusDtos = issueStatusService.getTeamAllIssueStatuses(teamId);
        return ResponseEntity.ok(issueStatusDtos);
    }
}
