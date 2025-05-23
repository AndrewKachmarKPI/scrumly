package com.scrumly.api.backlog;

import com.scrumly.dto.backlog.IssueDto;
import com.scrumly.dto.backlog.IssueExportOption;
import com.scrumly.service.backlog.BacklogIssueService;
import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
@Validated
public class IssueController {
    private final BacklogIssueService backlogIssueService;

    // Endpoint to get all issues with pagination and search
    @PostMapping("/search")
    public ResponseEntity<PageDto<IssueDto>> getIssues(@Valid @RequestBody SearchQuery searchQuery) {
        PageDto<IssueDto> issues = backlogIssueService.findIssues(searchQuery);
        return ResponseEntity.ok(issues);
    }

    // Endpoint to get issue by ID
    @GetMapping("/{id}")
    public ResponseEntity<IssueDto> getIssueById(@PathVariable Long id) {
        IssueDto issueDto = backlogIssueService.getIssueById(id);
        return ResponseEntity.ok(issueDto);
    }

    @GetMapping("/{key}/key")
    public ResponseEntity<IssueDto> getIssueByKey(@PathVariable String key) {
        IssueDto issueDto = backlogIssueService.getIssueByKey(key);
        return ResponseEntity.ok(issueDto);
    }


    // Endpoint to create a new issue
    @PostMapping
    public ResponseEntity<IssueDto> createIssue(@Valid @RequestBody IssueDto issueDto) {
        IssueDto createdIssue = backlogIssueService.createIssue(issueDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIssue);
    }

    // Endpoint to update an existing issue
    @PutMapping("/{id}")
    public ResponseEntity<IssueDto> updateIssue(@PathVariable Long id, @Valid @RequestBody IssueDto issueDto) {
        IssueDto updatedIssue = backlogIssueService.updateIssue(id, issueDto);
        return ResponseEntity.ok(updatedIssue);
    }

    // Endpoint to delete an issue by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssue(@PathVariable Long id) {
        backlogIssueService.deleteIssue(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{key}/export")
    public ResponseEntity<List<IssueExportOption>> getIssueExportOptions(@PathVariable String key) {
        return ResponseEntity.ok(backlogIssueService.getIssueExportOptions(key));
    }

    @PostMapping("/{issueKey}/export")
    public ResponseEntity<IssueDto> getIssueExportOptions(@PathVariable @NotNull String issueKey,
                                                          @RequestParam("description") @NotNull String description,
                                                          @Valid @RequestBody List<IssueExportOption> exportOption) {
        return ResponseEntity.ok(backlogIssueService.exportIssue(issueKey, description, exportOption));
    }
}
