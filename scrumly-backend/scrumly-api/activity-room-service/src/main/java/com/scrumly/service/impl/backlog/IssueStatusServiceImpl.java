package com.scrumly.service.impl.backlog;

import com.scrumly.domain.backlog.BacklogEntity;
import com.scrumly.domain.backlog.IssueStatusEntity;
import com.scrumly.dto.backlog.BacklogIssueStatusesDto;
import com.scrumly.dto.backlog.IssueStatusDto;
import com.scrumly.dto.user.TeamMetadataDto;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.feign.IntegrationServiceFeignClient;
import com.scrumly.feign.UserServiceFeignClient;
import com.scrumly.integration.jiraCloud.GetProjectPaginated;
import com.scrumly.integration.jiraCloud.SearchStatusesPaginated;
import com.scrumly.repository.backlog.BacklogRepository;
import com.scrumly.repository.backlog.IssueStatusRepository;
import com.scrumly.service.backlog.IssueStatusService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueStatusServiceImpl implements IssueStatusService {
    private final BacklogRepository backlogRepository;
    private final IssueStatusRepository issueStatusRepository;
    private final ModelMapper modelMapper;
    private final UserServiceFeignClient userServiceFeignClient;
    private final IntegrationServiceFeignClient integrationServiceFeignClient;

    @Override
    public List<IssueStatusEntity> createDefaultIssueStatus(String backlogId) {
        // List of default issue statuses with unique colors
        List<IssueStatusEntity> issueStatuses = Arrays.asList(
                IssueStatusEntity.builder()
                        .status("New")
                        .backlogId(backlogId)
                        .color("#A8D08D") // Light Green for New
                        .build(),
                IssueStatusEntity.builder()
                        .status("In Progress")
                        .backlogId(backlogId)
                        .color("#F4E041") // Soft Yellow for In Progress
                        .build(),
                IssueStatusEntity.builder()
                        .status("Closed")
                        .backlogId(backlogId)
                        .color("#FF6347") // Soft Red (Tomato) for Closed
                        .build(),
                IssueStatusEntity.builder()
                        .status("Blocked")
                        .backlogId(backlogId)
                        .color("#9B59B6") // Muted Purple for Blocked
                        .build(),
                IssueStatusEntity.builder()
                        .status("On Hold")
                        .backlogId(backlogId)
                        .color("#FFB84D") // Light Orange for On Hold
                        .build(),
                IssueStatusEntity.builder()
                        .status("Completed")
                        .backlogId(backlogId)
                        .color("#006400") // Deep Green for Completed
                        .build(),
                IssueStatusEntity.builder()
                        .status("Under Review")
                        .backlogId(backlogId)
                        .color("#4682B4") // Steel Blue for Under Review
                        .build()
        );


        // Save the issue statuses to the repository and return the saved entities
        return issueStatusRepository.saveAll(issueStatuses);
    }

    @Override
    public List<IssueStatusDto> getAllIssueStatuses(String backlogId) {
        return issueStatusRepository.findAllByBacklogId(backlogId).stream()
                .map(issueStatus -> modelMapper.map(issueStatus, IssueStatusDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<BacklogIssueStatusesDto> getTeamAllIssueStatuses(String teamId) {
        List<BacklogEntity> backlog = backlogRepository.findAllByTeamId(teamId);
        List<BacklogIssueStatusesDto> backlogIssueStatusesDtos = backlog.stream()
                .map(backlogEntity -> BacklogIssueStatusesDto.builder()
                        .backlogId(backlogEntity.getBacklogId())
                        .backlogName(backlogEntity.getName())
                        .statusList(backlogEntity.getIssueStatuses().stream()
                                            .map(issueStatusEntity -> IssueStatusDto.builder()
                                                    .id(issueStatusEntity.getId())
                                                    .backlogId(issueStatusEntity.getBacklogId())
                                                    .status(issueStatusEntity.getStatus())
                                                    .color(issueStatusEntity.getColor())
                                                    .statusId(issueStatusEntity.getId().toString())
                                                    .build())
                                            .toList())
                        .serviceType(ServiceType.SCRUMLY)
                        .build())
                .toList();
        List<BacklogIssueStatusesDto> statuses = new ArrayList<>(backlogIssueStatusesDtos);

        TeamMetadataDto teamMetadataDto = userServiceFeignClient.findTeamMetadata(teamId);
        try {
            if (teamMetadataDto != null) {
                GetProjectPaginated projectPaginated = integrationServiceFeignClient.getProjectPaginated(teamMetadataDto.getOrganizationId());

                List<BacklogIssueStatusesDto> jiraStatuses = jiraStatusesToBacklogIssueStatuses(projectPaginated.getValues(), teamMetadataDto.getOrganizationId());
                statuses.addAll(jiraStatuses);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statuses;
    }

    public List<BacklogIssueStatusesDto> jiraStatusesToBacklogIssueStatuses(List<GetProjectPaginated.GetProject> projects, String orgId) {
        List<BacklogIssueStatusesDto> statusesDtos = new ArrayList<>();
        for (GetProjectPaginated.GetProject project : projects) {
            SearchStatusesPaginated searchStatusesPaginated = integrationServiceFeignClient.searchStatusesPaginated(orgId, project.getId());
            List<IssueStatusDto> filteredStatuses = searchStatusesPaginated.getValues().stream()
                    .filter(status -> status.getScope() != null && status.getScope().getProject() != null)
                    .map(status -> IssueStatusDto.builder()
                            .backlogId(project.getId())
                            .status(status.getName())
                            .statusId(status.getId())
                            .build())
                    .toList();

            statusesDtos.add(BacklogIssueStatusesDto.builder()
                                     .backlogId(project.getId())
                                     .backlogName(project.getName())
                                     .statusList(new ArrayList<>())
                                     .serviceType(ServiceType.JIRA_CLOUD)
                                     .statusList(filteredStatuses)
                                     .build());
        }
        return statusesDtos;
    }


    @Override
    public List<IssueStatusDto> getAllIssueStatuses() {
        return issueStatusRepository.findAll().stream()
                .map(issueStatus -> modelMapper.map(issueStatus, IssueStatusDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public IssueStatusDto getIssueStatusById(Long id) {
        return issueStatusRepository.findById(id)
                .map(issueStatus -> modelMapper.map(issueStatus, IssueStatusDto.class))
                .orElseThrow(() -> new EntityNotFoundException("Issue status not found"));
    }

    @Override
    @Transactional
    public IssueStatusDto createIssueStatus(IssueStatusDto issueStatusDto) {
        if (issueStatusRepository.existsByStatusAndBacklogId(issueStatusDto.getStatus(), issueStatusDto.getBacklogId())) {
            throw new DuplicateEntityException("Issue status already exists");
        }
        IssueStatusEntity issueStatusEntity = modelMapper.map(issueStatusDto, IssueStatusEntity.class);
        issueStatusEntity = issueStatusRepository.save(issueStatusEntity);

        BacklogEntity backlog = getBacklog(issueStatusDto.getBacklogId());
        if (backlog.getIssueStatuses() == null) {
            backlog.setIssueStatuses(new ArrayList<>());
        }
        backlog.getIssueStatuses().add(issueStatusEntity);
        backlogRepository.save(backlog);

        return modelMapper.map(issueStatusEntity, IssueStatusDto.class);
    }

    @Override
    public IssueStatusDto updateIssueStatus(Long id, IssueStatusDto issueStatusDto) {
        if (!issueStatusRepository.existsById(id)) {
            throw new EntityNotFoundException("Issue status with id " + id + " does not exist.");
        }

        IssueStatusEntity issueStatusEntity = modelMapper.map(issueStatusDto, IssueStatusEntity.class);
        issueStatusEntity.setId(id);
        issueStatusEntity = issueStatusRepository.save(issueStatusEntity);

        return IssueStatusDto.builder()
                .id(issueStatusEntity.getId())
                .backlogId(issueStatusEntity.getBacklogId())
                .status(issueStatusEntity.getStatus())
                .color(issueStatusEntity.getColor())
                .build();
    }

    @Override
    @Transactional
    public void deleteIssueStatus(Long id) {
        IssueStatusEntity issueStatusEntity = issueStatusRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Issue status with id " + id + " does not exist."));

        BacklogEntity backlog = getBacklog(issueStatusEntity.getBacklogId());
        backlog.getIssueStatuses().removeIf(status -> status.getId().equals(issueStatusEntity.getId()));
        backlogRepository.save(backlog);

        issueStatusRepository.delete(issueStatusEntity);
    }

    @Override
    public IssueStatusEntity getIssueStatusByBacklogId(String backlogId, String status) {
        return issueStatusRepository.findByBacklogIdAndStatus(backlogId, status)
                .orElseThrow(() -> new EntityNotFoundException("Issue type with status " + status + " does not exist."));
    }

    @Override
    public IssueStatusEntity getIssueStatusByBacklogIdOrNull(String backlogId, String status) {
        return issueStatusRepository.findByBacklogIdAndStatus(backlogId, status).orElse(null);
    }

    private BacklogEntity getBacklog(String backlogId) {
        BacklogEntity backlog = backlogRepository.findByBacklogId(backlogId);
        if (backlog == null) {
            throw new EntityNotFoundException("Backlog is not found");
        }
        return backlog;
    }
}
