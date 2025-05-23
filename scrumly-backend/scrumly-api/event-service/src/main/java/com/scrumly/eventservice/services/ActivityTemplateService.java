package com.scrumly.eventservice.services;

import com.scrumly.eventservice.domain.ActivityTemplateEntity;
import com.scrumly.eventservice.dto.ActivityTemplateDto;
import com.scrumly.eventservice.dto.ActivityTemplateGroupDto;
import com.scrumly.eventservice.dto.requests.CreateActivityTemplateRQ;
import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ActivityTemplateService {
    ActivityTemplateDto createActivityTemplate(CreateActivityTemplateRQ createRQ, MultipartFile previewImg);

    ActivityTemplateDto createActivityTemplate(CreateActivityTemplateRQ createRQ);

    ActivityTemplateDto updateActivityTemplate(String templateId, ActivityTemplateDto templateRQ);

    ActivityTemplateDto updateActivityTemplate(String templateId, ActivityTemplateDto templateRQ, MultipartFile previewImg);

    ActivityTemplateDto copyActivityTemplate(String templateId, String ownerId);

    PageDto<ActivityTemplateDto> findActivityTemplates(SearchQuery searchQuery);

    PageDto<ActivityTemplateDto> findActivityTemplatesInfo(SearchQuery searchQuery);

    PageDto<ActivityTemplateDto> findMyActivityTemplates(SearchQuery searchQuery);
    List<ActivityTemplateGroupDto> findMyActivityTemplatesGroups(String ownerId);

    ActivityTemplateDto findActivityTemplateById(String templateId);

    ActivityTemplateDto findActivityTemplateByIdAndOwner(String templateId, String ownerId);

    void deleteActivityTemplate(String templateId, String ownerId);

    ActivityTemplateEntity getActivityTemplate(String templateId);
}
