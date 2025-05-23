package com.scrumly.integrationservice.service.jira;

import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.integrationservice.domain.jira.JiraAvailableResourceEntity;
import com.scrumly.integrationservice.domain.jira.JiraConnectionResourcesEntity;
import com.scrumly.integrationservice.dto.ServiceAuthorizeRQ;
import com.scrumly.integrationservice.dto.ServiceCredentialsDto;
import com.scrumly.integrationservice.dto.jiraCloud.GetAccessibleResourcesDto;
import com.scrumly.integrationservice.repository.jira.JiraConnectionResourcesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JiraConnectionResourceServiceImpl implements JiraConnectionResourceService {
    private final JiraConnectionResourcesRepository jiraConnectionResourcesRepository;

    @Override
    @Transactional
    public void saveConnectionRecourses(String connectionId, List<GetAccessibleResourcesDto> resources) {
        if (resources == null || resources.isEmpty()) {
            throw new EntityNotFoundException("Jira cloud resources are not available");
        }

        JiraConnectionResourcesEntity connectionResourcesEntity = jiraConnectionResourcesRepository.findByConnectionId(connectionId);
        if (connectionResourcesEntity == null) {
            connectionResourcesEntity = new JiraConnectionResourcesEntity();
        }

        List<JiraAvailableResourceEntity> jiraResources = resources.stream()
                .map(getAccessibleResourcesDto -> JiraAvailableResourceEntity.builder()
                        .resourceId(getAccessibleResourcesDto.getId())
                        .name(getAccessibleResourcesDto.getName())
                        .url(getAccessibleResourcesDto.getUrl())
                        .scopes(getAccessibleResourcesDto.getScopes())
                        .avatarUrl(getAccessibleResourcesDto.getAvatarUrl())
                        .build())
                .toList();
        connectionResourcesEntity = connectionResourcesEntity.toBuilder()
                .connectionId(connectionId)
                .selectedResource(jiraResources.get(0))
                .resources(jiraResources)
                .build();
        jiraConnectionResourcesRepository.save(connectionResourcesEntity);
    }

    @Override
    public JiraAvailableResourceEntity getConnectionSelectedResource(String connectionId) {
        JiraConnectionResourcesEntity connectionResourcesEntity = jiraConnectionResourcesRepository.findByConnectionId(connectionId);
        if (connectionResourcesEntity == null) {
            throw new EntityNotFoundException("Jira resources are not found");
        }
        return connectionResourcesEntity.getSelectedResource();
    }
}
