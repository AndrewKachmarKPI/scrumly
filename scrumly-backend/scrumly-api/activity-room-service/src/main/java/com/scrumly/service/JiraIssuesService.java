package com.scrumly.service;

import com.scrumly.dto.issues.IssueShortInfo;

import java.util.List;

public interface JiraIssuesService {
    List<IssueShortInfo> getIssuePickerSuggestions(String connectingId, String query);
    List<IssueShortInfo> getTopIssues(String connectingId, Integer topLimit);

}
