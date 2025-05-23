package com.scrumly.eventservice.services.impl;

import com.scrumly.eventservice.domain.ActivityTypeEntity;
import com.scrumly.eventservice.dto.ActivityTypeDto;
import com.scrumly.eventservice.repository.ActivityTypeRepository;
import com.scrumly.eventservice.services.ActivityTypeService;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityTypeServiceImpl implements ActivityTypeService {
    private final ActivityTypeRepository repository;
    private final ModelMapper modelMapper;

    @Override
    public ActivityTypeDto create(ActivityTypeDto dto) {
        if (repository.existsByType(dto.getType())) {
            throw new DuplicateEntityException("Such activity type already exists!");
        }
        ActivityTypeEntity entity = modelMapper.map(dto, ActivityTypeEntity.class);
        entity.setDateTimeCreated(LocalDateTime.now());
        ActivityTypeEntity savedEntity = repository.save(entity);
        return modelMapper.map(savedEntity, ActivityTypeDto.class);
    }

    @Override
    public ActivityTypeDto update(Long id, ActivityTypeDto dto) {
        ActivityTypeEntity existingEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ActivityType not found with id: " + id));
        existingEntity.setType(dto.getType());
        ActivityTypeEntity updatedEntity = repository.save(existingEntity);
        return modelMapper.map(updatedEntity, ActivityTypeDto.class);
    }

    @Override
    public ActivityTypeDto getById(Long id) {
        ActivityTypeEntity entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ActivityType not found with id: " + id));
        return modelMapper.map(entity, ActivityTypeDto.class);
    }

    @Override
    public ActivityTypeEntity getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ActivityType not found with id: " + id));
    }

    @Override
    public ActivityTypeEntity getEntityByType(String type) {
        return repository.findByType(type)
                .orElseThrow(() -> new EntityNotFoundException("ActivityType not found with type: " + type));
    }

    @Override
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("ActivityType not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public List<ActivityTypeDto> getAll() {
        return repository.findAll()
                .stream()
                .map(entity -> modelMapper.map(entity, ActivityTypeDto.class))
                .collect(Collectors.toList());
    }
}
