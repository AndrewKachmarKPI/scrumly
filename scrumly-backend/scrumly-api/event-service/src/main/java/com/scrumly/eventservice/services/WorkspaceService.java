package com.scrumly.eventservice.services;

import com.scrumly.eventservice.domain.activity.ActivityEntity;
import com.scrumly.eventservice.domain.workspace.WorkspaceEntity;
import com.scrumly.eventservice.dto.workspace.WorkspaceDto;

import java.util.List;

public interface WorkspaceService {
    WorkspaceEntity createWorkspace();

    WorkspaceDto findWorkspace(String workspaceId);

    WorkspaceDto createWorkspaceConference(String workspaceId);

    void appendActivities(String workspaceId, List<ActivityEntity> activityIds);

    void removeActivities(String workspaceId, List<ActivityEntity> activityIds);

    void deleteWorkspace(WorkspaceEntity workspace);

    void deleteWorkspaceActivity(String workspaceId, String activityId);
}
