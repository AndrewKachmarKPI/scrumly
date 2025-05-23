package com.scrumly.integrationservice.service.jira;

import com.scrumly.integrationservice.dto.jiraCloud.*;
import com.scrumly.integrationservice.messaging.dto.ExportIssueDto;

import java.util.List;

public interface JiraCloudApiService {
    List<GetAccessibleResourcesDto> getAccessibleResources(String connectingId);

    GetIssuePickerSuggestions getIssuePickerSuggestions(String connectingId, String query);

    GetSearchIssuesEnhanced getSearchIssuesEnhanced(String connectingId, String query);
    GetSearchIssuesEnhanced getSearchIssuesEnhancedTop(String connectingId, Integer limit);

    GetIssue getIssue(String connectingId, String key);

    List<GetIssue> getIssues(String connectingId, List<String> keys);

    void exportIssue(String connectingId, ExportIssueDto exportIssueDto);

    GetProjectPaginated getProjectPaginated(String connectingId);
    List<GetAllIssueTypesForProject> getAllIssueTypesForProject(String connectingId, String projectId);

    GetIssue exportIssue(String connectingId, com.scrumly.integration.ExportIssueDto exportIssueDto);

    List<GetBulkStatuses> getBulkStatuses(String connectingId);

    SearchStatusesPaginated searchStatusesPaginated(String connectingId, String projectId);

}
