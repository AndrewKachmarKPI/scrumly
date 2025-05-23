package com.scrumly.service.impl.backlog;

import com.scrumly.domain.backlog.AssigneeEntity;
import com.scrumly.domain.backlog.BacklogEntity;
import com.scrumly.domain.backlog.IssueEntity;
import com.scrumly.domain.backlog.IssueEstimationEntity;
import com.scrumly.domain.backlog.IssueExportRefEntity;
import com.scrumly.domain.backlog.IssueStatusEntity;
import com.scrumly.dto.backlog.BacklogDto;
import com.scrumly.dto.backlog.IssueDto;
import com.scrumly.dto.backlog.IssueExportOption;
import com.scrumly.dto.issues.IssueShortInfo;
import com.scrumly.dto.user.TeamMetadataDto;
import com.scrumly.dto.user.UserProfileDto;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.feign.IntegrationServiceFeignClient;
import com.scrumly.feign.UserServiceFeignClient;
import com.scrumly.integration.jiraCloud.GetAllIssueTypesForProject;
import com.scrumly.integration.jiraCloud.GetIssue;
import com.scrumly.integration.jiraCloud.GetProjectPaginated;
import com.scrumly.mappers.BusinessMapper;
import com.scrumly.messaging.dto.ExportIssueDto;
import com.scrumly.repository.backlog.AssigneeRepository;
import com.scrumly.repository.backlog.BacklogRepository;
import com.scrumly.repository.backlog.IssueRepository;
import com.scrumly.service.backlog.BacklogIssueService;
import com.scrumly.service.backlog.BacklogService;
import com.scrumly.service.backlog.IssueStatusService;
import com.scrumly.service.backlog.IssueTypeService;
import com.scrumly.specification.CompareOption;
import com.scrumly.specification.GeneralSpecification;
import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchFilter;
import com.scrumly.specification.SearchOperators;
import com.scrumly.specification.SearchQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.scrumly.utils.SecurityUtils.getUsername;

@Slf4j
@Service
@RequiredArgsConstructor
public class BacklogIssueServiceImpl implements BacklogIssueService {
    private final IssueRepository issueRepository;
    private final ModelMapper modelMapper;
    private final BacklogRepository backlogRepository;
    private final IssueTypeService issueTypeService;
    private final IssueStatusService issueStatusService;
    private final UserServiceFeignClient userServiceFeignClient;
    private final IntegrationServiceFeignClient integrationServiceFeignClient;
    private final AssigneeRepository assigneeRepository;
    private final BusinessMapper businessMapper;
    private final BacklogService backlogService;

    @Override
    public List<IssueShortInfo> searchIssues(String teamId, String query) {
        List<String> backlogs = backlogRepository.findAllByTeamId(teamId).stream()
                .map(BacklogEntity::getBacklogId)
                .toList();
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.appendSearchFilter(new SearchFilter("title", SearchOperators.LIKE, CompareOption.AND, query));
        searchQuery.appendSearchFilter(new SearchFilter("backlogId", SearchOperators.IN, CompareOption.AND, backlogs));
        searchQuery.setPageSize(1000);
        searchQuery.setPageNumber(0);
        PageDto<IssueDto> issues = findIssues(searchQuery);
        return businessMapper.getIssueShortInfo(issues.getData());
    }

    @Override
    public List<IssueShortInfo> loadTopIssues(String teamId, Integer topLimit) {
        List<String> backlogs = backlogRepository.findAllByTeamId(teamId).stream()
                .map(BacklogEntity::getBacklogId)
                .toList();
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.appendSearchFilter(new SearchFilter("backlogId", SearchOperators.IN, CompareOption.AND, backlogs));
        searchQuery.setPageSize(topLimit);
        searchQuery.setPageNumber(0);
        PageDto<IssueDto> issues = findIssues(searchQuery);
        return businessMapper.getIssueShortInfo(issues.getData());
    }

    @Override
    public PageDto<IssueDto> findIssues(SearchQuery searchQuery) {
        Specification<IssueEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        Page<IssueEntity> page = issueRepository.findAll(specification, pageable);
        return GeneralSpecification.getPageResponse(page, issueEntity -> modelMapper.map(issueEntity, IssueDto.class));
    }

    @Override
    public List<IssueDto> findIssues(List<String> keys) {
        List<IssueEntity> issues = issueRepository.findAllByIssueKeyIn(keys);
        return issues.stream()
                .map(issueEntity -> modelMapper.map(issueEntity, IssueDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IssueDto createIssue(IssueDto issueDto) {
        BacklogEntity backlog = null;
        if (issueDto.getBacklogId() != null) {
            backlog = getBacklog(issueDto.getBacklogId());
        } else if (issueDto.getTeamId() != null) {
            backlog = getBacklogByTeamId(issueDto.getTeamId());
        } else {
            throw new EntityNotFoundException("Backlog is not found");
        }

        IssueEntity issueEntity = modelMapper.map(issueDto, IssueEntity.class);


        issueEntity.setIssueKey(backlog.getIssueIdentifier() + "-" + (backlog.getIssues().size() + 1));
        issueEntity.setCreatedBy(getAssignee(null));
        issueEntity.setCreatedDateTime(LocalDateTime.now());
        issueEntity.setIssueType(issueTypeService.getIssueTypeByBacklogId(backlog.getBacklogId(), issueDto.getIssueType().getType()));
        issueEntity.setStatus(issueStatusService.getIssueStatusByBacklogId(backlog.getBacklogId(), issueDto.getStatus().getStatus()));
        issueEntity.setAssignee(getAssignee(issueDto.getAssignee() != null
                                                    ? issueDto.getAssignee().getUserId()
                                                    : null));
        issueEntity = issueRepository.save(issueEntity);

        if (backlog.getIssues() == null) {
            backlog.setIssues(new ArrayList<>());
        }
        backlog.getIssues().add(issueEntity);
        backlogRepository.save(backlog);

        return modelMapper.map(issueEntity, IssueDto.class);
    }

    @Override
    @Transactional
    public IssueDto updateIssue(Long id, IssueDto issueDto) {
        // Check if the issue exists
        IssueEntity issueEntity = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found with id: " + id));

        if (issueDto.getTitle() != null) {
            issueEntity.setTitle(issueDto.getTitle());
        }
        if (issueDto.getDescription() != null) {
            issueEntity.setDescription(issueDto.getDescription());
        }

        // Check if the backlog exists for the issue and update the backlog references
        BacklogEntity backlog = getBacklog(issueDto.getBacklogId());
        // Ensure the issue type and status are set correctly
        if (issueDto.getIssueType() != null) {
            issueEntity.setIssueType(issueTypeService.getIssueTypeByBacklogId(backlog.getBacklogId(), issueDto.getIssueType().getType()));
        }
        if (issueDto.getStatus() != null) {
            issueEntity.setStatus(issueStatusService.getIssueStatusByBacklogId(backlog.getBacklogId(), issueDto.getStatus().getStatus()));
        }
        if (issueDto.getAssignee() != null) {
            issueEntity.setAssignee(getAssignee(issueDto.getAssignee().getUserId()));
        }
        if (issueDto.getIssueEstimation() != null) {
            IssueEstimationEntity estimation = modelMapper.map(issueDto.getIssueEstimation(), IssueEstimationEntity.class);
            issueEntity.setIssueEstimationEntity(estimation);
        }
        // Map the other fields from the issueDto

        // Save the updated entity
        issueEntity = issueRepository.save(issueEntity);

        // Return the updated IssueDto
        return modelMapper.map(issueEntity, IssueDto.class);
    }

    @Override
    @Transactional
    public void deleteIssue(Long id) {
        IssueEntity existingIssue = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found with id: " + id));

        BacklogEntity backlog = getBacklog(existingIssue.getBacklogId());
        backlog.getIssues().removeIf(issueEntity -> issueEntity.getIssueKey().equals(existingIssue.getIssueKey()));
        backlogRepository.save(backlog);

        // Delete the issue by its ID
        issueRepository.delete(existingIssue);
    }

    @Override
    public IssueDto getIssueById(Long id) {
        // Fetch the issue from the repository by its ID
        IssueEntity issueEntity = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found with id: " + id));

        // Convert the found IssueEntity to IssueDto and return it
        return modelMapper.map(issueEntity, IssueDto.class);
    }

    @Override
    public IssueDto getIssueByKey(String key) {
        // Fetch the issue from the repository by its ID
        IssueEntity issueEntity = issueRepository.findByIssueKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found with key: " + key));

        BacklogEntity backlog = backlogRepository.findByBacklogId(issueEntity.getBacklogId());

        // Convert the found IssueEntity to IssueDto and return it
        IssueDto issueDto = modelMapper.map(issueEntity, IssueDto.class);
        issueDto.setBacklogName(backlog.getName());
        return issueDto;
    }

    @Override
    public void exportEstimation(ExportIssueDto exportIssueDto) {
        List<String> keys = exportIssueDto.getIssues()
                .stream()
                .map(ExportIssueDto.IssueChangesDto::getIssueId)
                .toList();
        List<IssueEntity> issueEntities = issueRepository.findAllByIssueKeyIn(keys);
        for (IssueEntity issueEntity : issueEntities) {
            ExportIssueDto.IssueChangesDto issueDto = exportIssueDto.getIssues().stream()
                    .filter(issueChangesDto -> issueChangesDto.getIssueId().equals(issueEntity.getIssueKey()))
                    .findAny()
                    .orElse(null);
            if (issueDto == null) {
                continue;
            }
            if (issueDto.getEstimation() != null) {
                issueEntity.setIssueEstimationEntity(IssueEstimationEntity.builder()
                                                             .estimation(issueDto.getEstimation())
                                                             .activityName(exportIssueDto.getActivityName())
                                                             .activityId(exportIssueDto.getActivityId())
                                                             .build());
            }
            if (issueDto.getStatus() != null && issueDto.getBacklogId() != null) {
                IssueStatusEntity status = issueStatusService.getIssueStatusByBacklogIdOrNull(issueDto.getBacklogId(), issueDto.getStatus());
                if (status != null) {
                    issueEntity.setStatus(status);
                }
            }
            if (issueDto.getAssigneeId() != null) {
                issueEntity.setAssignee(getAssignee(issueDto.getAssigneeId()));
            }
            issueRepository.save(issueEntity);
            log.info("ISSUE: {} UPDATED: {}", issueDto.getIssueId(), issueEntity);
        }
    }

    @Override
    public List<IssueExportOption> getIssueExportOptions(String issueKey) {
        IssueEntity issueEntity = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found with key: " + issueKey));
        BacklogEntity backlog = backlogRepository.findByBacklogId(issueEntity.getBacklogId());
        TeamMetadataDto teamMetadata = userServiceFeignClient.findTeamMetadata(backlog.getTeamId());
        List<ServiceType> connectedServices = integrationServiceFeignClient.findConnectedServices(teamMetadata.getOrganizationId());
        List<IssueExportOption> exportOptions = new ArrayList<>();
        for (ServiceType connectedService : connectedServices) {
            if (connectedService.equals(ServiceType.JIRA_CLOUD)) {
                GetProjectPaginated projectPaginated = integrationServiceFeignClient.getProjectPaginated(teamMetadata.getOrganizationId());
                List<IssueExportOption> options = projectPaginated.getValues().stream()
                        .map(getProject -> {
                            List<GetAllIssueTypesForProject> issueTypes = integrationServiceFeignClient.getAllIssueTypesForProject(teamMetadata.getOrganizationId(), getProject.getId());
                            List<IssueExportOption.ProjectIssueType> projectIssueTypes = issueTypes.stream()
                                    .map(issueType -> IssueExportOption.ProjectIssueType.builder()
                                            .iconUrl(issueType.getIconUrl())
                                            .id(issueType.getId())
                                            .name(issueType.getName())
                                            .build())
                                    .toList();
                            return IssueExportOption.builder()
                                    .serviceType(ServiceType.JIRA_CLOUD)
                                    .organizationId(teamMetadata.getOrganizationId())
                                    .projectId(getProject.getId())
                                    .projectName(getProject.getName())
                                    .issueKey(issueKey)
                                    .issueTypes(projectIssueTypes)
                                    .issueType(!projectIssueTypes.isEmpty() ? projectIssueTypes.get(0) : null)
                                    .build();
                        })
                        .toList();
                exportOptions.addAll(options);
            }
        }
        return exportOptions;
    }

    @Override
    public IssueDto exportIssue(String issueKey, String description, List<IssueExportOption> exportOptions) {
        IssueEntity issueEntity = issueRepository.findByIssueKey(issueKey)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found with key: " + issueKey));

        List<IssueExportRefEntity> exportRefs = new ArrayList<>();
        for (IssueExportOption exportOption : exportOptions) {
            if (exportOption.getServiceType().equals(ServiceType.JIRA_CLOUD)) {
                com.scrumly.integration.ExportIssueDto exportIssueDto = com.scrumly.integration.ExportIssueDto.builder()
                        .description(description)
                        .title(issueEntity.getTitle())
                        .projectId(exportOption.getProjectId())
                        .issueTypeId(exportOption.getIssueType().getId())
                        .build();
                GetIssue getIssue = integrationServiceFeignClient.exportIssueJira(exportOption.getOrganizationId(), exportIssueDto);
                IssueExportRefEntity ref = IssueExportRefEntity.builder()
                        .serviceName(ServiceType.JIRA_CLOUD.name())
                        .projectName(exportOption.getProjectName())
                        .serviceIssueKey(getIssue.getKey())
                        .issueUrl(getIssue.getInternalFields().getUrl())
                        .issueTypeUrl(exportOption.getIssueType().getIconUrl())
                        .issueTypeName(exportOption.getIssueType().getName())
                        .exportedDate(LocalDateTime.now())
                        .build();
                exportRefs.add(ref);
            }
        }

        if (issueEntity.getExportRefs() == null) {
            issueEntity.setExportRefs(new ArrayList<>());
        }
        issueEntity.getExportRefs().addAll(exportRefs);

        issueEntity = issueRepository.save(issueEntity);
        return modelMapper.map(issueEntity, IssueDto.class);
    }

    private BacklogEntity getBacklog(String backlogId) {
        BacklogEntity backlog = backlogRepository.findByBacklogId(backlogId);
        if (backlog == null) {
            throw new EntityNotFoundException("Backlog is not found");
        }
        return backlog;
    }

    private BacklogEntity getBacklogByTeamId(String teamId) {
        List<BacklogEntity> backlogs = backlogRepository.findAllByTeamId(teamId);
        if (backlogs.isEmpty()) {
            BacklogDto backlogDto = backlogService.createBacklogDefault(teamId);
            return backlogRepository.findByBacklogId(backlogDto.getBacklogId());
        }
        return backlogs.get(0);
    }

    private AssigneeEntity getAssignee(String userId) {
        String username = userId == null ? getUsername() : userId;
        AssigneeEntity assignee = assigneeRepository.findByUserId(username);
        if (assignee != null) {
            return assignee;
        }
        UserProfileDto userProfileDto = userServiceFeignClient.findUserProfile(username).getBody();
        assignee = AssigneeEntity.builder()
                .email(userProfileDto.getEmail())
                .firstName(userProfileDto.getFirstName())
                .lastName(userProfileDto.getLastName())
                .userId(userProfileDto.getUserId())
                .build();
        return assigneeRepository.save(assignee);
    }
}
