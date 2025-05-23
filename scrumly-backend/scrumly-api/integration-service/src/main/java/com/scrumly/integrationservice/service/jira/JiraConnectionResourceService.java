package com.scrumly.integrationservice.service.jira;

import com.scrumly.integrationservice.domain.jira.JiraAvailableResourceEntity;
import com.scrumly.integrationservice.dto.jiraCloud.GetAccessibleResourcesDto;

import java.util.List;

public interface JiraConnectionResourceService {
    void saveConnectionRecourses(String connectionId, List<GetAccessibleResourcesDto> resources);
    JiraAvailableResourceEntity getConnectionSelectedResource(String connectionId);
}
