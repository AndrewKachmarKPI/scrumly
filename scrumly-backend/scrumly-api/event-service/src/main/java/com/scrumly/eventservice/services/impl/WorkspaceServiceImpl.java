package com.scrumly.eventservice.services.impl;

import com.scrumly.eventservice.domain.activity.ActivityEntity;
import com.scrumly.eventservice.domain.workspace.WorkspaceEntity;
import com.scrumly.eventservice.dto.workspace.WorkspaceDto;
import com.scrumly.eventservice.feign.ConferenceServiceFeignClient;
import com.scrumly.eventservice.mapper.BusinessMapper;
import com.scrumly.eventservice.repository.WorkspaceEntityRepository;
import com.scrumly.eventservice.services.WorkspaceService;
import com.scrumly.exceptions.types.ServiceErrorException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.scrumly.eventservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {
    private final WorkspaceEntityRepository workspaceRepository;
    private final ConferenceServiceFeignClient conferenceServiceClient;
    private final ModelMapper modelMapper;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WorkspaceEntity createWorkspace() {
        String workspaceId = UUID.randomUUID().toString();
        String conferenceId = null;
        try {
            conferenceId = conferenceServiceClient.createWorkspaceConference(workspaceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        WorkspaceEntity workspace = WorkspaceEntity.builder()
                .workspaceId(workspaceId)
                .conferenceId(conferenceId)
                .createdBy(getUsername())
                .createdAt(LocalDateTime.now())
                .build();
        return workspaceRepository.save(workspace);
    }

    @Override
    public WorkspaceDto createWorkspaceConference(String workspaceId) {
        WorkspaceEntity workspace = getWorkspaceByIdOrThrow(workspaceId);
        if (workspace.getConferenceId() != null) {
            throw new ServiceErrorException("Conference already created");
        }
        try {
            String conferenceId = conferenceServiceClient.createWorkspaceConference(workspaceId);
            workspace.setConferenceId(conferenceId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
        workspace = workspaceRepository.save(workspace);
        return businessMapper.workspaceToDtoWithActivities(workspace);
    }

    @Override
    public WorkspaceDto findWorkspace(String workspaceId) {
        WorkspaceEntity workspace = getWorkspaceByIdOrThrow(workspaceId);
        return businessMapper.workspaceToDtoWithActivities(workspace);
    }

    @Override
    public void appendActivities(String workspaceId, List<ActivityEntity> activityIds) {
        WorkspaceEntity workspace = getWorkspaceByIdOrThrow(workspaceId);
        workspace.getActivities().addAll(activityIds);
        workspaceRepository.save(workspace);
    }

    @Override
    public void removeActivities(String workspaceId, List<ActivityEntity> activityIds) {
        WorkspaceEntity workspace = getWorkspaceByIdOrThrow(workspaceId);
        workspace.getActivities().removeIf(activity -> activityIds.stream().anyMatch(activity1 -> activity1.getActivityId().equals(activity.getActivityId())));
        workspaceRepository.save(workspace);
    }

    @Override
    @Transactional
    public void deleteWorkspace(WorkspaceEntity workspace) {
        if (workspace != null) {
            try {
                conferenceServiceClient.deleteConference(workspace.getConferenceId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            workspaceRepository.delete(workspace);
        }
    }

    @Override
    @Transactional
    public void deleteWorkspaceActivity(String workspaceId, String activityId) {
        WorkspaceEntity workspace = workspaceRepository.findByWorkspaceId(workspaceId);
        if (workspace == null) {
            throw new EntityNotFoundException("Workspace is not found");
        }
//        workspace.removeActivity(activityId);
//        if (workspace.getActivities().isEmpty()) {
//            workspaceRepository.delete(workspace);
//        } else {
//            workspaceRepository.save(workspace);
//        }
    }

    private WorkspaceEntity getWorkspaceByIdOrThrow(String workspaceId) {
        WorkspaceEntity workspace = workspaceRepository.findByWorkspaceId(workspaceId);
        if (workspace == null) {
            throw new EntityNotFoundException("Workspace is not found");
        }
        return workspace;
    }
}
