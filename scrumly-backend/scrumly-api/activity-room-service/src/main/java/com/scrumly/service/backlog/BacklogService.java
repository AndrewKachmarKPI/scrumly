package com.scrumly.service.backlog;

import com.scrumly.dto.backlog.BacklogDto;
import com.scrumly.dto.user.UserProfileDto;

import java.util.List;

public interface BacklogService {
    List<BacklogDto> getTeamBacklogs(String teamId);

    BacklogDto createBacklogDefault(String teamId);

    BacklogDto createBacklog(String teamId, BacklogDto backlogDto);

    Boolean hasBacklog(String teamId);

    BacklogDto getBacklogById(String backlogId);

    List<UserProfileDto> getAllAssignee(String backlogId);
}
