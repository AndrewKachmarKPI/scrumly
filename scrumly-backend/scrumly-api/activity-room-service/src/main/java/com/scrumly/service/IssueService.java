package com.scrumly.service;

import com.scrumly.dto.issues.IssueShortInfo;
import com.scrumly.enums.integration.ServiceType;

import java.util.List;

public interface IssueService {
    List<IssueShortInfo> searchIssues(ServiceType serviceType, String connectingId, String query);
    List<IssueShortInfo> loadTopIssues(ServiceType serviceType, String connectingId,  Integer topLimit);
}
