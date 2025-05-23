package com.scrumly.service.impl;

import com.scrumly.dto.issues.IssueShortInfo;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.feign.IntegrationServiceFeignClient;
import com.scrumly.integration.jiraCloud.GetSearchIssuesEnhanced;
import com.scrumly.mappers.BusinessMapper;
import com.scrumly.service.JiraIssuesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JiraIssuesServiceImpl implements JiraIssuesService {
    private final IntegrationServiceFeignClient integrationServiceFeignClient;
    private final BusinessMapper businessMapper;

    @Override
    public List<IssueShortInfo> getIssuePickerSuggestions(String connectingId, String query) {
        List<IssueShortInfo> issueShortInfos = new ArrayList<>();
        try {
            GetSearchIssuesEnhanced suggestions = integrationServiceFeignClient.getSearchIssuesEnhanced(connectingId, query);
            issueShortInfos = businessMapper.getIssueShortInfoEnhances(suggestions, ServiceType.JIRA_CLOUD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return issueShortInfos;
    }

    @Override
    public List<IssueShortInfo> getTopIssues(String connectingId, Integer topLimit) {
        List<IssueShortInfo> issueShortInfos = new ArrayList<>();
        try {
            GetSearchIssuesEnhanced suggestions = integrationServiceFeignClient.getSearchIssuesEnhancedTop(connectingId, topLimit);
            issueShortInfos = businessMapper.getIssueShortInfoEnhances(suggestions, ServiceType.JIRA_CLOUD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return issueShortInfos;
    }
}
