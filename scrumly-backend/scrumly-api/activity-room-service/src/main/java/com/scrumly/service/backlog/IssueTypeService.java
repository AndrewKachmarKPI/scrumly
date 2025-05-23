package com.scrumly.service.backlog;

import com.scrumly.domain.backlog.IssueTypeEntity;
import com.scrumly.dto.backlog.IssueTypeDto;

import java.util.List;

public interface IssueTypeService {
    List<IssueTypeEntity> createDefaultIssueTypes(String backlogId);

    List<IssueTypeDto> getAllIssuesType();
    List<IssueTypeDto> getAllIssuesType(String backlogId);


    IssueTypeDto getIssueTypeById(Long id);

    IssueTypeDto createIssueType(IssueTypeDto issueTypeDTO);

    IssueTypeDto updateIssueType(Long id, IssueTypeDto issueTypeDTO);

    void deleteIssueType(Long id);

    IssueTypeEntity getIssueTypeByBacklogId(String backlogId, String type);
}
