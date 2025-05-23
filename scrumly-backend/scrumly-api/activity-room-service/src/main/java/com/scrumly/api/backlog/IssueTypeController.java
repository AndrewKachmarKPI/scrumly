package com.scrumly.api.backlog;

import com.scrumly.dto.backlog.IssueTypeDto;
import com.scrumly.service.backlog.IssueTypeService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/issue-types")
@RequiredArgsConstructor
@Validated
public class IssueTypeController {

    private final IssueTypeService issueTypeService;

    @GetMapping("/{backlogId}/all")
    public ResponseEntity<List<IssueTypeDto>> getIssueTypeByBacklogId(@PathVariable("backlogId") String backlogId) {
        List<IssueTypeDto> issueTypes = issueTypeService.getAllIssuesType(backlogId);
        return ResponseEntity.ok(issueTypes);
    }

    @GetMapping
    public ResponseEntity<List<IssueTypeDto>> getAllIssueTypes() {
        List<IssueTypeDto> issueTypes = issueTypeService.getAllIssuesType();
        return ResponseEntity.ok(issueTypes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueTypeDto> getIssueTypeById(@PathVariable Long id) {
        IssueTypeDto issueType = issueTypeService.getIssueTypeById(id);
        return ResponseEntity.ok(issueType);
    }

    @PostMapping
    public ResponseEntity<IssueTypeDto> createIssueType(@Valid @RequestBody IssueTypeDto issueTypeDto) {
        IssueTypeDto createdIssueType = issueTypeService.createIssueType(issueTypeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIssueType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IssueTypeDto> updateIssueType(@PathVariable Long id, @Valid @RequestBody IssueTypeDto issueTypeDto) {
        IssueTypeDto updatedIssueType = issueTypeService.updateIssueType(id, issueTypeDto);
        return ResponseEntity.ok(updatedIssueType);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssueType(@PathVariable Long id) {
        issueTypeService.deleteIssueType(id);
        return ResponseEntity.noContent().build();
    }
}