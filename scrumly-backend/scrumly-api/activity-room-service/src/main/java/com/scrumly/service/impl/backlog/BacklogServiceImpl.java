package com.scrumly.service.impl.backlog;

import com.scrumly.domain.backlog.BacklogEntity;
import com.scrumly.dto.backlog.BacklogDto;
import com.scrumly.dto.user.TeamMetadataDto;
import com.scrumly.dto.user.UserProfileDto;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.feign.UserServiceFeignClient;
import com.scrumly.repository.backlog.BacklogRepository;
import com.scrumly.service.backlog.BacklogService;
import com.scrumly.service.backlog.IssueStatusService;
import com.scrumly.service.backlog.IssueTypeService;
import com.scrumly.utils.RandomIdentifierGenerator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BacklogServiceImpl implements BacklogService {
    private final BacklogRepository backlogRepository;
    private final ModelMapper modelMapper;
    private final UserServiceFeignClient userServiceFeignClient;
    private final IssueTypeService issueTypeService;
    private final IssueStatusService issueStatusService;

    @Override
    public List<BacklogDto> getTeamBacklogs(String teamId) {
        return backlogRepository.findAllByTeamId(teamId)
                .stream()
                .map(backlogEntity -> modelMapper.map(backlogEntity, BacklogDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public BacklogDto createBacklogDefault(String teamId) {
        if (backlogRepository.existsByTeamId(teamId)) {
            throw new DuplicateEntityException("Backlog already exists");
        }

        TeamMetadataDto teamMetadataDto = userServiceFeignClient.findTeamMetadata(teamId);
        String backlogId = UUID.randomUUID().toString();
        BacklogEntity backlog = BacklogEntity.builder()
                .name(teamMetadataDto.getTeamName() + " -  Backlog")
                .createdDateTime(LocalDateTime.now())
                .backlogId(backlogId)
                .issues(new ArrayList<>())
                .issueIdentifier(RandomIdentifierGenerator.generateRandomIssueIdentifier())
                .teamId(teamMetadataDto.getTeamId())
                .issueTypes(issueTypeService.createDefaultIssueTypes(backlogId))
                .issueStatuses(issueStatusService.createDefaultIssueStatus(backlogId))
                .build();
        backlog = backlogRepository.save(backlog);
        return modelMapper.map(backlog, BacklogDto.class);
    }

    @Override
    @Transactional
    public BacklogDto createBacklog(String teamId, BacklogDto backlogDto) {
        TeamMetadataDto teamMetadataDto = userServiceFeignClient.findTeamMetadata(teamId);
        String backlogId = UUID.randomUUID().toString();
        BacklogEntity backlog = BacklogEntity.builder()
                .name(backlogDto.getName())
                .issueIdentifier(backlogDto.getIssueIdentifier())
                .createdDateTime(LocalDateTime.now())
                .backlogId(backlogId)
                .teamId(teamMetadataDto.getTeamId())
                .issues(new ArrayList<>())
                .issueTypes(issueTypeService.createDefaultIssueTypes(backlogId))
                .issueStatuses(issueStatusService.createDefaultIssueStatus(backlogId))
                .build();
        backlog = backlogRepository.save(backlog);
        return modelMapper.map(backlog, BacklogDto.class);
    }

    @Override
    public Boolean hasBacklog(String teamId) {
        return backlogRepository.existsByTeamId(teamId);
    }

    @Override
    public BacklogDto getBacklogById(String backlogId) {
        BacklogEntity backlog = backlogRepository.findByBacklogId(backlogId);
        if (backlog == null) {
            throw new EntityNotFoundException("Backlog is not found");
        }
        return modelMapper.map(backlog, BacklogDto.class);
    }

    @Override
    public List<UserProfileDto> getAllAssignee(String backlogId) {
        BacklogEntity backlog = backlogRepository.findByBacklogId(backlogId);
        if (backlog == null) {
            throw new EntityNotFoundException("Backlog is not found");
        }
        return userServiceFeignClient.findTeamUsers(backlog.getTeamId());
    }
}
