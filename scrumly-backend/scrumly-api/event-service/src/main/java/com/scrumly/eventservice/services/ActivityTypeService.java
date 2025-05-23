package com.scrumly.eventservice.services;

import com.scrumly.eventservice.domain.ActivityTypeEntity;
import com.scrumly.eventservice.dto.ActivityTypeDto;

import java.util.List;

public interface ActivityTypeService {
    ActivityTypeDto create(ActivityTypeDto dto);
    ActivityTypeDto update(Long id, ActivityTypeDto dto);
    ActivityTypeDto getById(Long id);
    ActivityTypeEntity getEntityById(Long id);
    ActivityTypeEntity getEntityByType(String type);
    void deleteById(Long id);
    List<ActivityTypeDto> getAll();
}
