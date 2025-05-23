package com.scrumly.integrationservice.api;

import com.scrumly.integration.ExportIssueDto;
import com.scrumly.integrationservice.dto.jiraCloud.GetAllIssueTypesForProject;
import com.scrumly.integrationservice.dto.jiraCloud.GetBulkStatuses;
import com.scrumly.integrationservice.dto.jiraCloud.GetIssue;
import com.scrumly.integrationservice.dto.jiraCloud.GetIssuePickerSuggestions;
import com.scrumly.integrationservice.dto.jiraCloud.GetProjectPaginated;
import com.scrumly.integrationservice.dto.jiraCloud.GetSearchIssuesEnhanced;
import com.scrumly.integrationservice.dto.jiraCloud.SearchStatusesPaginated;
import com.scrumly.integrationservice.service.jira.JiraCloudApiService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/jira/cloud")
@RequiredArgsConstructor
@Validated
public class JiraCloudApiController {
    private final JiraCloudApiService jiraCloudApiService;


    @GetMapping("/issue/picker")
    public ResponseEntity<GetIssuePickerSuggestions> getIssuePickerSuggestions(@RequestParam("connectingId") @NotBlank String connectingId,
                                                                               @RequestParam("query") @NotBlank String query) {
        return ResponseEntity.ok(jiraCloudApiService.getIssuePickerSuggestions(connectingId, query));
    }

    @GetMapping("/issue/search")
    public ResponseEntity<GetSearchIssuesEnhanced> getSearchIssuesEnhanced(@RequestParam("connectingId") @NotBlank String connectingId,
                                                                           @RequestParam("query") @NotBlank String query) {
        return ResponseEntity.ok(jiraCloudApiService.getSearchIssuesEnhanced(connectingId, query));
    }

    @GetMapping("/issue/search/top")
    public ResponseEntity<GetSearchIssuesEnhanced> getSearchIssuesEnhancedTop(@RequestParam("connectingId") @NotBlank String connectingId,
                                                                              @RequestParam("limit") @NotNull Integer limit) {
        return ResponseEntity.ok(jiraCloudApiService.getSearchIssuesEnhancedTop(connectingId, limit));
    }

    @GetMapping("/issue")
    public ResponseEntity<GetIssue> getIssue(@RequestParam("connectingId") @NotBlank String connectingId,
                                             @RequestParam("key") @NotBlank String key) {
        return ResponseEntity.ok(jiraCloudApiService.getIssue(connectingId, key));
    }

    @GetMapping("/issues")
    public ResponseEntity<List<GetIssue>> getIssues(@RequestParam("connectingId") @NotBlank String connectingId,
                                                    @RequestParam("keys") List<@NotBlank String> keys) {
        return ResponseEntity.ok(jiraCloudApiService.getIssues(connectingId, keys));
    }

    @GetMapping("/projects")
    public ResponseEntity<GetProjectPaginated> getProjectPaginated(@RequestParam("connectingId") @NotBlank String connectingId) {
        return ResponseEntity.ok(jiraCloudApiService.getProjectPaginated(connectingId));
    }

    @GetMapping("/issue-types")
    public ResponseEntity<List<GetAllIssueTypesForProject>> getAllIssueTypesForProject(@RequestParam("connectingId") @NotBlank String connectingId,
                                                                                       @RequestParam("projectId") @NotBlank String projectId) {
        return ResponseEntity.ok(jiraCloudApiService.getAllIssueTypesForProject(connectingId, projectId));
    }


    @PostMapping("/issue/export")
    public ResponseEntity<GetIssue> exportIssue(@RequestParam("connectingId") @NotBlank String connectingId,
                                                @RequestBody ExportIssueDto exportIssueDto) {
        return ResponseEntity.ok(jiraCloudApiService.exportIssue(connectingId, exportIssueDto));
    }

    @GetMapping("/issue/statuses")
    public ResponseEntity<List<GetBulkStatuses>> getBulkStatuses(@RequestParam("connectingId") @NotBlank String connectingId) {
        return ResponseEntity.ok(jiraCloudApiService.getBulkStatuses(connectingId));
    }

    @GetMapping("/issue/statuses-search")
    public ResponseEntity<SearchStatusesPaginated> searchStatusesPaginated(@RequestParam("connectingId") @NotBlank String connectingId,
                                                                           @RequestParam("projectId") @NotBlank String projectId) {
        return ResponseEntity.ok(jiraCloudApiService.searchStatusesPaginated(connectingId, projectId));
    }
}
