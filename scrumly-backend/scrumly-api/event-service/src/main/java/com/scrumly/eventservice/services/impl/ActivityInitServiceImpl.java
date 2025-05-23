package com.scrumly.eventservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scrumly.eventservice.domain.ActivityTypeEntity;
import com.scrumly.eventservice.dto.ActivityTemplateDto;
import com.scrumly.eventservice.dto.requests.CreateActivityTemplateRQ;
import com.scrumly.eventservice.repository.ActivityTemplateRepository;
import com.scrumly.eventservice.repository.ActivityTypeRepository;
import com.scrumly.eventservice.services.ActivityInitService;
import com.scrumly.eventservice.services.ActivityTemplateService;
import com.scrumly.exceptions.types.ServiceErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ActivityInitServiceImpl implements ActivityInitService {
    private final ActivityTemplateService templateService;
    private final ActivityTypeRepository activityTypeRepository;
    private final ActivityTemplateRepository activityTemplateRepository;

    private final ObjectMapper objectMapper;

    public ActivityInitServiceImpl(ActivityTypeRepository activityTypeRepository,
                                   ActivityTemplateService templateService,
                                   ActivityTemplateRepository activityTemplateRepository) {
        this.activityTypeRepository = activityTypeRepository;
        this.templateService = templateService;
        this.activityTemplateRepository = activityTemplateRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }


    @Value("${default-path.activity-types}")
    private String activityTypePath;
    @Value("${default-path.activity-templates}")
    private String activityTemplatePath;

    @Override
    public void initDefaultActivity() {
        try {
            initActivityTypes();
            initActivityTemplates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initActivityTypes() {
        try {
            ClassPathResource resource = new ClassPathResource(activityTypePath);
            ActivityTypeEntity[] activityTypes = objectMapper.readValue(resource.getInputStream(), ActivityTypeEntity[].class);
            for (ActivityTypeEntity config : activityTypes) {
                if (!activityTypeRepository.existsByType(config.getType())) {
                    activityTypeRepository.save(config);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initActivityTemplates() {
        try {
            ClassPathResource resource = new ClassPathResource(activityTemplatePath);
            CreateActivityTemplateRQ[] createTemplateRQ = objectMapper.readValue(resource.getInputStream(), CreateActivityTemplateRQ[].class);
            for (CreateActivityTemplateRQ rq : createTemplateRQ) {
                if (activityTemplateRepository.existsByName(rq.getName())) {
                    continue;
                }
                templateService.createActivityTemplate(rq);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
