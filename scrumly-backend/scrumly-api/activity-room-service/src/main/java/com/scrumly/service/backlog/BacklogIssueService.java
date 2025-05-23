package com.scrumly.service.backlog;

import com.scrumly.dto.backlog.IssueDto;
import com.scrumly.dto.backlog.IssueExportOption;
import com.scrumly.dto.issues.IssueShortInfo;
import com.scrumly.messaging.dto.ExportIssueDto;
import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;

import java.util.List;

public interface BacklogIssueService {
    List<IssueShortInfo> searchIssues(String teamId, String query);
    List<IssueShortInfo> loadTopIssues(String teamId, Integer topLimit);
    PageDto<IssueDto> findIssues(SearchQuery searchQuery);

    List<IssueDto> findIssues(List<String> keys);

    IssueDto createIssue(IssueDto issueDto);

    IssueDto updateIssue(Long id, IssueDto issueDto);

    void deleteIssue(Long id);

    IssueDto getIssueById(Long id);
    IssueDto getIssueByKey(String id);

    void exportEstimation(ExportIssueDto exportIssueDto);

    List<IssueExportOption> getIssueExportOptions(String issueKey);

    IssueDto exportIssue(String issueKey, String description, List<IssueExportOption> issueExportOptions);
}
