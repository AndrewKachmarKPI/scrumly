package com.scrumly.feign;

import com.scrumly.config.FeignClientConfig;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.integration.ExportIssueDto;
import com.scrumly.integration.googleGemini.GeminiApiRequest;
import com.scrumly.integration.googleGemini.GeminiApiResponse;
import com.scrumly.integration.jiraCloud.GetAllIssueTypesForProject;
import com.scrumly.integration.jiraCloud.GetBulkStatuses;
import com.scrumly.integration.jiraCloud.GetIssue;
import com.scrumly.integration.jiraCloud.GetIssuePickerSuggestions;
import com.scrumly.integration.jiraCloud.GetProjectPaginated;
import com.scrumly.integration.jiraCloud.GetSearchIssuesEnhanced;
import com.scrumly.integration.jiraCloud.SearchStatusesPaginated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "integration-service", configuration = FeignClientConfig.class)
public interface IntegrationServiceFeignClient {
    @GetMapping("/services/is-connected/user")
    ResponseEntity<Boolean> isConnected(@RequestParam("serviceType") ServiceType serviceType);

    @GetMapping("/services/connected")
    List<ServiceType> findConnectedServices(@RequestParam("connectionId") String connectionId);

    @GetMapping("/api/jira/cloud/issue/picker")
    GetIssuePickerSuggestions getJiraIssuePickerSuggestions(@RequestParam("connectingId") @NotBlank String connectingId,
                                                            @RequestParam("query") @NotNull String query);

    @GetMapping("/api/jira/cloud/issue/search")
    GetSearchIssuesEnhanced getSearchIssuesEnhanced(@RequestParam("connectingId") @NotBlank String connectingId,
                                                    @RequestParam("query") @NotBlank String query);

    @GetMapping("/api/jira/cloud/issue/search/top")
    GetSearchIssuesEnhanced getSearchIssuesEnhancedTop(@RequestParam("connectingId") @NotBlank String connectingId,
                                                       @RequestParam("limit") @NotBlank Integer limit);

    @GetMapping("/issue")
    GetIssue getIssue(@RequestParam("connectingId") @NotBlank String connectingId,
                      @RequestParam("key") @NotBlank String key);

    @GetMapping("/api/jira/cloud/issues")
    List<GetIssue> getIssues(@RequestParam("connectingId") @NotBlank String connectingId,
                             @RequestParam("keys") List<@NotBlank String> keys);

    @GetMapping("/api/jira/cloud/projects")
    GetProjectPaginated getProjectPaginated(@RequestParam("connectingId") @NotBlank String connectingId);

    @GetMapping("/api/jira/cloud/issue-types")
    List<GetAllIssueTypesForProject> getAllIssueTypesForProject(@RequestParam("connectingId") @NotBlank String connectingId,
                                                                @RequestParam("projectId") @NotBlank String projectId);

    @PostMapping("/api/jira/cloud/issue/export")
    GetIssue exportIssueJira(@RequestParam("connectingId") @NotBlank String connectingId,
                             @RequestBody ExportIssueDto exportIssueDto);

    @GetMapping("/api/jira/cloud/issue/statuses")
    List<GetBulkStatuses> getBulkStatuses(@RequestParam("connectingId") @NotBlank String connectingId);

    @GetMapping("/api/jira/cloud/issue/statuses-search")
    SearchStatusesPaginated searchStatusesPaginated(@RequestParam("connectingId") @NotBlank String connectingId,
                                                    @RequestParam("projectId") @NotBlank String projectId);

    @PostMapping("/google/gemini/generateContent")
    GeminiApiResponse generateContent(@Valid @RequestBody GeminiApiRequest apiRequest);
}
