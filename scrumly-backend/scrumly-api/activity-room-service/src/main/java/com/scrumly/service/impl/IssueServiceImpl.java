package com.scrumly.service.impl;

import com.scrumly.dto.issues.IssueShortInfo;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.service.IssueService;
import com.scrumly.service.JiraIssuesService;
import com.scrumly.service.backlog.BacklogIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {
    private final JiraIssuesService jiraIssuesService;
    private final BacklogIssueService backlogIssueService;

    @Override
    public List<IssueShortInfo> searchIssues(ServiceType serviceType, String connectingId, String query) {
        List<IssueShortInfo> issues = new ArrayList<>();
        if (serviceType.equals(ServiceType.JIRA_CLOUD)) {
            issues = jiraIssuesService.getIssuePickerSuggestions(connectingId, query);
        } else if (serviceType.equals(ServiceType.SCRUMLY)) {
            issues = backlogIssueService.searchIssues(connectingId, query);
        }
        return issues;
    }

    @Override
    public List<IssueShortInfo> loadTopIssues(ServiceType serviceType, String connectingId, Integer topLimit) {
        List<IssueShortInfo> issues = new ArrayList<>();
        if (serviceType.equals(ServiceType.JIRA_CLOUD)) {
            issues = jiraIssuesService.getTopIssues(connectingId, topLimit);
        } else if (serviceType.equals(ServiceType.SCRUMLY)) {
            issues = backlogIssueService.loadTopIssues(connectingId, topLimit);
        }
        return issues;
    }
}
