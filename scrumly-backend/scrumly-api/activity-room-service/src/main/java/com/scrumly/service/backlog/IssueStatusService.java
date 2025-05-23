package com.scrumly.service.backlog;

import com.scrumly.domain.backlog.IssueStatusEntity;
import com.scrumly.dto.backlog.BacklogIssueStatusesDto;
import com.scrumly.dto.backlog.IssueStatusDto;

import java.util.List;

public interface IssueStatusService {
    List<IssueStatusEntity> createDefaultIssueStatus(String backlogId);

    List<IssueStatusDto> getAllIssueStatuses(String backlogId);

    List<BacklogIssueStatusesDto> getTeamAllIssueStatuses(String teamId);

    List<IssueStatusDto> getAllIssueStatuses();

    IssueStatusDto getIssueStatusById(Long id);

    IssueStatusDto createIssueStatus(IssueStatusDto issueStatusDto);

    IssueStatusDto updateIssueStatus(Long id, IssueStatusDto issueStatusDto);

    void deleteIssueStatus(Long id);

    IssueStatusEntity getIssueStatusByBacklogId(String backlogId, String status);
    IssueStatusEntity getIssueStatusByBacklogIdOrNull(String backlogId, String status);

}
