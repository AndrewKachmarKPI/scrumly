package com.scrumly.service.impl.backlog;

import com.scrumly.domain.backlog.BacklogEntity;
import com.scrumly.domain.backlog.IssueStatusEntity;
import com.scrumly.domain.backlog.IssueTypeEntity;
import com.scrumly.dto.backlog.IssueTypeDto;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.repository.backlog.BacklogRepository;
import com.scrumly.repository.backlog.IssueTypeRepository;
import com.scrumly.service.backlog.IssueTypeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueTypeServiceImpl implements IssueTypeService {
    private final IssueTypeRepository issueTypeRepository;
    private final BacklogRepository backlogRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<IssueTypeEntity> createDefaultIssueTypes(String backlogId) {
        List<IssueTypeEntity> issueTypes = Arrays.asList(
                IssueTypeEntity.builder()
                        .type("Story")
                        .iconUrl("assets/data/story-type/story_icon.png")
                        .isDefault(true)
                        .backlogId(backlogId)
                        .build(),
                IssueTypeEntity.builder()
                        .type("Issue")
                        .iconUrl("assets/data/story-type/issue_icon.png")
                        .isDefault(true)
                        .backlogId(backlogId)
                        .build(),
                IssueTypeEntity.builder()
                        .type("Bug")
                        .iconUrl("assets/data/story-type/bug_icon.png")
                        .isDefault(true)
                        .backlogId(backlogId)
                        .build(),
                IssueTypeEntity.builder()
                        .type("Task")
                        .iconUrl("assets/data/story-type/task_icon.png")
                        .isDefault(true)
                        .backlogId(backlogId)
                        .build()
        );
        return issueTypeRepository.saveAll(issueTypes);
    }

    @Override
    public List<IssueTypeDto> getAllIssuesType() {
        return issueTypeRepository.findAll().stream()
                .map(issue -> modelMapper.map(issue, IssueTypeDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<IssueTypeDto> getAllIssuesType(String backlogId) {
        return issueTypeRepository.findAllByBacklogId(backlogId).stream()
                .map(issue -> modelMapper.map(issue, IssueTypeDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public IssueTypeDto getIssueTypeById(Long id) {
        return issueTypeRepository.findById(id)
                .map(issue -> modelMapper.map(issue, IssueTypeDto.class))
                .orElseThrow(() -> new EntityNotFoundException("Issue type with id " + id + " is not found"));
    }

    @Override
    @Transactional
    public IssueTypeDto createIssueType(IssueTypeDto typeDto) {
        IssueTypeEntity issueTypeEntity = modelMapper.map(typeDto, IssueTypeEntity.class);
        if (issueTypeRepository.existsByBacklogIdAndType(typeDto.getBacklogId(), typeDto.getType())) {
            throw new DuplicateEntityException("Issue type already exists");
        }
        issueTypeEntity = issueTypeRepository.save(issueTypeEntity);

        BacklogEntity backlog = getBacklog(typeDto.getBacklogId());
        if (backlog.getIssueTypes() == null) {
            backlog.setIssueTypes(new ArrayList<>());
        }
        backlog.getIssueTypes().add(issueTypeEntity);
        backlogRepository.save(backlog);

        return modelMapper.map(issueTypeEntity, IssueTypeDto.class);
    }

    @Override
    public IssueTypeDto updateIssueType(Long id, IssueTypeDto issueTypeDto) {
        IssueTypeEntity existingEntity = issueTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue type with id " + id + " does not exist."));

        // Update fields as necessary
        existingEntity.setType(issueTypeDto.getType());
        if (issueTypeDto.getIconUrl() != null) {
            existingEntity.setIconUrl(issueTypeDto.getIconUrl());
        }
        existingEntity = issueTypeRepository.save(existingEntity);  // Save updated entity

        return modelMapper.map(existingEntity, IssueTypeDto.class);
    }

    @Override
    @Transactional
    public void deleteIssueType(Long id) {
        IssueTypeEntity issueTypeEntity = issueTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Issue type with id " + id + " does not exist."));

        BacklogEntity backlog = getBacklog(issueTypeEntity.getBacklogId());
        backlog.getIssueTypes().removeIf(type -> type.getId().equals(issueTypeEntity.getId()));
        backlogRepository.save(backlog);

        issueTypeRepository.delete(issueTypeEntity);
    }

    @Override
    public IssueTypeEntity getIssueTypeByBacklogId(String backlogId, String type) {
        return issueTypeRepository.findByBacklogIdAndType(backlogId, type)
                .orElseThrow(() -> new EntityNotFoundException("Issue type with type " + type + " does not exist."));
    }

    private BacklogEntity getBacklog(String backlogId) {
        BacklogEntity backlog = backlogRepository.findByBacklogId(backlogId);
        if (backlog == null) {
            throw new EntityNotFoundException("Backlog is not found");
        }
        return backlog;
    }
}
