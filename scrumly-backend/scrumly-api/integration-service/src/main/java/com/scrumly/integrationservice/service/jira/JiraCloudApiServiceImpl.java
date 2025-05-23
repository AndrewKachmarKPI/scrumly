package com.scrumly.integrationservice.service.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.integrationservice.domain.jira.JiraAvailableResourceEntity;
import com.scrumly.integrationservice.dto.ServiceCredentialsDto;
import com.scrumly.integrationservice.dto.jiraCloud.*;
import com.scrumly.integrationservice.enums.jira.CustomFieldConfig;
import com.scrumly.integrationservice.messaging.dto.ExportIssueDto;
import com.scrumly.integrationservice.service.ServiceCredentialsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraCloudApiServiceImpl implements JiraCloudApiService {
    private final WebClient jiraCloudApiClient;
    private final JiraCloudAuthService jiraCloudAuthService;
    private final ServiceCredentialsService credentialsService;
    private final JiraConnectionResourceService jiraConnectionResourceService;

    @Value("${integration.jira-cloud.api-url}")
    private String jiraCloudApiUrl;

    private List<String> CUSTOM_FIELDS_KEY = Arrays.asList(
            "Story point estimate", CustomFieldConfig.SCRUMLY_ESTIMATE.getName()
    );

    @Override
    public List<GetAccessibleResourcesDto> getAccessibleResources(String connectingId) {
        ServiceCredentialsDto credentialsDto = getCredentials(connectingId);
        List<GetAccessibleResourcesDto> rs = null;
        try {
            rs = jiraCloudApiClient.get()
                    .uri("/oauth/token/accessible-resources")
                    .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GetAccessibleResourcesDto>>() {
                    })
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
        return rs;
    }

    @Override
    public GetIssuePickerSuggestions getIssuePickerSuggestions(String connectingId, String query) {
        ServiceCredentialsDto credentialsDto = getCredentials(connectingId);
        JiraAvailableResourceEntity resource = getJiraResource(connectingId);
        GetIssuePickerSuggestions rs = null;
        try {
            rs = sendGetIssuePickerSuggestions(query, resource.getResourceId(), credentialsDto);
        } catch (WebClientResponseException.Unauthorized e) {
            credentialsDto = updateCredentials(credentialsDto);
            try {
                rs = sendGetIssuePickerSuggestions(query, resource.getResourceId(), credentialsDto);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new ServiceErrorException(ex);
            }
        }
        for (GetIssuePickerSuggestions.Section section : rs.getSections()) {
            for (GetIssuePickerSuggestions.Issue issue : section.getIssues()) {
                issue.setImg(jiraCloudApiUrl + "/ex/jira/" + resource.getResourceId() + issue.getImg());
                issue.setUrl(resource.getUrl() + "/browse/" + issue.getKey());
            }
        }
        return rs;
    }

    private GetIssuePickerSuggestions sendGetIssuePickerSuggestions(String query, String cloudId, ServiceCredentialsDto credentialsDto) {
        GetIssuePickerSuggestions rs;
        String currentJQL = "currentJQL=text ~ " + query + " OR issueKey ~ " + query;
        rs = jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/issue/picker?query=" + query + "&currentJQL=" + currentJQL)
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GetIssuePickerSuggestions>() {
                })
                .block();
        return rs;
    }

    @Override
    public GetSearchIssuesEnhanced getSearchIssuesEnhanced(String connectingId, String query) {
        ServiceCredentialsDto credentialsDto = getCredentials(connectingId);
        JiraAvailableResourceEntity resource = getJiraResource(connectingId);
        GetSearchIssuesEnhanced rs = null;
        try {
            rs = sendGetSearchIssuesEnhanced(query, resource.getResourceId(), credentialsDto);
        } catch (WebClientResponseException.Unauthorized e) {
            credentialsDto = updateCredentials(credentialsDto);
            try {
                rs = sendGetSearchIssuesEnhanced(query, resource.getResourceId(), credentialsDto);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new ServiceErrorException(ex);
            }
        }
        for (GetSearchIssuesEnhanced.SearchIssue issue : rs.getIssues()) {
            issue.setUrl(resource.getUrl() + "/browse/" + issue.getKey());
        }
        return rs;
    }

    private GetSearchIssuesEnhanced sendGetSearchIssuesEnhanced(String query,
                                                                String cloudId,
                                                                ServiceCredentialsDto credentialsDto) {
        GetSearchIssuesEnhanced rs;
        String fields = "summary, issuetype, project, status";
        String jql = "text ~ " + query + " OR issueKey ~ " + query + " OR summary ~ " + query;
        String maxResults = "5000";
        rs = getGetSearchIssuesEnhanced(cloudId, credentialsDto, jql, maxResults, fields);
        return rs;
    }

    @Override
    public GetSearchIssuesEnhanced getSearchIssuesEnhancedTop(String connectingId, Integer limit) {
        ServiceCredentialsDto credentialsDto = getCredentials(connectingId);
        JiraAvailableResourceEntity resource = getJiraResource(connectingId);
        GetSearchIssuesEnhanced rs = null;
        try {
            rs = sendGetSearchIssuesEnhancedTop(limit, resource.getResourceId(), credentialsDto);
        } catch (WebClientResponseException.Unauthorized e) {
            credentialsDto = updateCredentials(credentialsDto);
            try {
                rs = sendGetSearchIssuesEnhancedTop(limit, resource.getResourceId(), credentialsDto);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new ServiceErrorException(ex);
            }
        }
        for (GetSearchIssuesEnhanced.SearchIssue issue : rs.getIssues()) {
            issue.setUrl(resource.getUrl() + "/browse/" + issue.getKey());
        }
        return rs;
    }

    private GetSearchIssuesEnhanced sendGetSearchIssuesEnhancedTop(Integer limit,
                                                                   String cloudId,
                                                                   ServiceCredentialsDto credentialsDto) {
        GetSearchIssuesEnhanced rs;
        String fields = "summary, issuetype, project, status";
        String jql = "summary is not EMPTY";
        String maxResults = limit.toString();
        rs = getGetSearchIssuesEnhanced(cloudId, credentialsDto, jql, maxResults, fields);
        return rs;
    }

    private GetSearchIssuesEnhanced getGetSearchIssuesEnhanced(String cloudId, ServiceCredentialsDto credentialsDto, String jql, String maxResults, String fields) {
        GetSearchIssuesEnhanced rs;
        rs = jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/search/jql?jql=" + jql +
                             "&maxResults=" + maxResults +
                             "&fields=" + fields)
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GetSearchIssuesEnhanced>() {
                })
                .block();
        return rs;
    }

    @Override
    public GetIssue getIssue(String connectingId, String key) {
        ServiceCredentialsDto credentialsDto = getCredentials(connectingId);
        JiraAvailableResourceEntity resource = getJiraResource(connectingId);
        GetIssue rs = null;
        try {
            rs = sendGetIssue(key, resource.getResourceId(), credentialsDto);
        } catch (WebClientResponseException.Unauthorized e) {
            credentialsDto = updateCredentials(credentialsDto);
            try {
                rs = sendGetIssue(key, resource.getResourceId(), credentialsDto);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new ServiceErrorException(ex);
            }
        }
        return rs;
    }

    private GetIssue sendGetIssue(String key, String cloudId, ServiceCredentialsDto credentialsDto) {
        GetIssue rs;
        String expand = "expand=renderedFields";
        rs = jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/issue/" + key + "?" + expand)
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GetIssue>() {
                })
                .block();
        return rs;
    }

    @Override
    public GetProjectPaginated getProjectPaginated(String connectingId) {
        ServiceCredentialsDto credentialsDto = getCredentials(connectingId);
        JiraAvailableResourceEntity resource = getJiraResource(connectingId);
        GetProjectPaginated rs = null;
        try {
            rs = sendProjectPaginated(resource.getResourceId(), credentialsDto);
        } catch (WebClientResponseException.Unauthorized e) {
            credentialsDto = updateCredentials(credentialsDto);
            try {
                rs = sendProjectPaginated(resource.getResourceId(), credentialsDto);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new ServiceErrorException(ex);
            }
        }
        return rs;
    }

    private GetProjectPaginated sendProjectPaginated(String cloudId, ServiceCredentialsDto credentialsDto) {
        GetProjectPaginated rs;
        rs = jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/project/search")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GetProjectPaginated>() {
                })
                .doOnNext(response -> log.info("Jira API Response: {}", response))
                .block();
        return rs;
    }

    @Override
    public GetIssue exportIssue(String connectingId, com.scrumly.integration.ExportIssueDto exportIssueDto) {
        ServiceCredentialsDto credentialsDto = getCredentials(connectingId);
        JiraAvailableResourceEntity resource = getJiraResource(connectingId);
        GetIssue rs = null;
        CreateIssue createIssue = CreateIssue.builder()
                .fields(CreateIssue.CreateIssueFields.builder()
                                .summary(exportIssueDto.getTitle())
                                .description(CreateIssue.CreateIssueFields.DescriptionField.builder()
                                                     .content(Arrays.asList(
                                                             CreateIssue.CreateIssueFields.DescriptionField.ContentWrapper.builder()
                                                                     .content(Arrays.asList(
                                                                             CreateIssue.CreateIssueFields.DescriptionField.ContentItem.builder()
                                                                                     .text(exportIssueDto.getDescription())
                                                                                     .type("text")
                                                                                     .build()
                                                                     ))
                                                                     .type("paragraph")
                                                                     .build()
                                                     ))
                                                     .type("doc")
                                                     .version(1)
                                                     .build())
                                .project(CreateIssue.CreateIssueFields.ProjectField.builder()
                                                 .id(exportIssueDto.getProjectId())
                                                 .build())
                                .issuetype(CreateIssue.CreateIssueFields.IssueTypeField.builder()
                                                   .id(exportIssueDto.getIssueTypeId())
                                                   .build())
                                .build())
                .build();
        try {
            rs = sendCreateIssue(resource.getResourceId(), credentialsDto, createIssue);
        } catch (WebClientResponseException.Unauthorized e) {
            credentialsDto = updateCredentials(credentialsDto);
            try {
                rs = sendCreateIssue(resource.getResourceId(), credentialsDto, createIssue);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new ServiceErrorException(ex);
            }
        }
        if (rs != null) {
            if (rs.getInternalFields() == null) {
                rs.setInternalFields(new GetIssue.InternalFields());
            }
            rs.getInternalFields().setUrl(resource.getUrl() + "/browse/" + rs.getKey());
        }
        return rs;
    }

    @Override
    public List<GetBulkStatuses> getBulkStatuses(String connectingId) {
        ServiceCredentialsDto credentialsDto = getCredentials(connectingId);
        JiraAvailableResourceEntity resource = getJiraResource(connectingId);
        List<GetBulkStatuses> rs = null;
        try {
            rs = sendGetBulkStatuses(resource.getResourceId(), credentialsDto);
        } catch (WebClientResponseException.Unauthorized e) {
            credentialsDto = updateCredentials(credentialsDto);
            try {
                rs = sendGetBulkStatuses(resource.getResourceId(), credentialsDto);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new ServiceErrorException(ex);
            }
        }
        return rs;
    }

    private List<GetBulkStatuses> sendGetBulkStatuses(String cloudId, ServiceCredentialsDto credentialsDto) {
        List<GetBulkStatuses> rs;
        rs = jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/statuses")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GetBulkStatuses>>() {
                })
                .doOnNext(response -> log.info("Jira API Response: {}", response))
                .block();
        return rs;
    }

    @Override
    public SearchStatusesPaginated searchStatusesPaginated(String connectingId, String projectId) {
        ServiceCredentialsDto credentialsDto = getCredentials(connectingId);
        JiraAvailableResourceEntity resource = getJiraResource(connectingId);
        SearchStatusesPaginated rs = null;
        try {
            rs = sendSearchStatusesPaginated(resource.getResourceId(), credentialsDto, projectId);
        } catch (WebClientResponseException.Unauthorized e) {
            credentialsDto = updateCredentials(credentialsDto);
            try {
                rs = sendSearchStatusesPaginated(resource.getResourceId(), credentialsDto, projectId);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new ServiceErrorException(ex);
            }
        }
        return rs;
    }

    private SearchStatusesPaginated sendSearchStatusesPaginated(String cloudId, ServiceCredentialsDto credentialsDto, String projectId) {
        SearchStatusesPaginated rs;
        rs = jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/statuses/search?projectId=" + projectId)
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<SearchStatusesPaginated>() {
                })
                .doOnNext(response -> log.info("Jira API Response: {}", response))
                .block();
        return rs;
    }


    @Override
    public List<GetAllIssueTypesForProject> getAllIssueTypesForProject(String connectingId, String projectId) {
        ServiceCredentialsDto credentialsDto = getCredentials(connectingId);
        JiraAvailableResourceEntity resource = getJiraResource(connectingId);
        List<GetAllIssueTypesForProject> rs = null;
        try {
            rs = sendGetIssueTypeForProject(resource.getResourceId(), credentialsDto, projectId);
        } catch (WebClientResponseException.Unauthorized e) {
            credentialsDto = updateCredentials(credentialsDto);
            try {
                rs = sendGetIssueTypeForProject(resource.getResourceId(), credentialsDto, projectId);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new ServiceErrorException(ex);
            }
        }
        return rs;
    }

    private GetIssue sendCreateIssue(String cloudId,
                                     ServiceCredentialsDto credentialsDto,
                                     CreateIssue createIssue) {
        GetIssue rs;
        rs = jiraCloudApiClient.post()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/issue")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .body(BodyInserters.fromValue(createIssue))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GetIssue>() {
                })
                .doOnNext(response -> log.info("Jira API Response: {}", response))
                .block();
        return rs;
    }

    private List<GetAllIssueTypesForProject> sendGetIssueTypeForProject(String cloudId,
                                                                        ServiceCredentialsDto credentialsDto,
                                                                        String projectId) {
        return jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/issuetype/project?" + "projectId=" + projectId)
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GetAllIssueTypesForProject>>() {
                })
                .block();
    }

    private List<GetField> sendGetFields(String cloudId, ServiceCredentialsDto credentialsDto) {
        return jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/field")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GetField>>() {
                })
                .block();
    }

    private List<GetField> sendSearchFields(String query, List<String> projectIds, String cloudId, ServiceCredentialsDto credentialsDto) {
        List<GetField> rs;
        String params = "projectIds=" + String.join(",", projectIds) + "&maxResults=" + 5000;
        if (query != null) {
            params += "&query=" + query;
        }
        GetAllFields fields = jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/field/search" + "?" + params)
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GetAllFields>() {
                })
                .block();
        return fields.getValues();
    }


    private GetField sendCreateField(CreateField createField, String cloudId, ServiceCredentialsDto credentialsDto) {
        GetField rs;
        rs = jiraCloudApiClient.post()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/field")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .body(BodyInserters.fromValue(createField))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GetField>() {
                })
                .block();
        return rs;
    }

    private void sendDeleteField(String fieldId, String cloudId, ServiceCredentialsDto credentialsDto) {
        jiraCloudApiClient.delete()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/field/" + fieldId)
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Void>() {
                })
                .block();
    }

    private void sendDeleteFieldConfiguration(String fieldConfigurationId, String cloudId, ServiceCredentialsDto credentialsDto) {
        jiraCloudApiClient.delete()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/fieldconfiguration/" + fieldConfigurationId)
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Void>() {
                })
                .block();
    }

    private void sendDeleteFieldConfigurationScheme(String fieldConfigurationScheme, String cloudId, ServiceCredentialsDto credentialsDto) {
        jiraCloudApiClient.delete()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/fieldconfigurationscheme/" + fieldConfigurationScheme)
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Void>() {
                })
                .block();
    }

    private GetAllFieldsConfigurations sendGetAllFieldsConfigurations(String cloudId, ServiceCredentialsDto credentialsDto) {
        GetAllFieldsConfigurations rs;
        rs = jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/fieldconfiguration")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GetAllFieldsConfigurations>() {
                })
                .block();
        return rs;
    }

    private FieldConfiguration sendCreateFieldConfiguration(CreateFieldConfiguration fieldConfiguration,
                                                            String cloudId, ServiceCredentialsDto credentialsDto) {
        FieldConfiguration rs;
        rs = jiraCloudApiClient.post()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/fieldconfiguration")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .body(BodyInserters.fromValue(fieldConfiguration))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<FieldConfiguration>() {
                })
                .block();
        return rs;
    }


    private GetFieldConfigurationItems sendGetAllFieldsConfigurations(String cloudId,
                                                                      String fieldConfigurationId,
                                                                      ServiceCredentialsDto credentialsDto) {
        GetFieldConfigurationItems rs;
        rs = jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/fieldconfiguration/" + fieldConfigurationId + "/fields")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GetFieldConfigurationItems>() {
                })
                .block();
        return rs;
    }

    private void sendUpdateFieldConfigurationItems(UpdateFieldConfigurationItems updateFieldConfigurationItems,
                                                   String fieldConfigurationId,
                                                   String cloudId, ServiceCredentialsDto credentialsDto) {
        FieldConfiguration rs;
        jiraCloudApiClient.put()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/fieldconfiguration/" + fieldConfigurationId + "/fields")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .body(BodyInserters.fromValue(updateFieldConfigurationItems))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Void>() {
                })
                .block();
    }


    private GetAllFieldConfigurationSchemes sendGetAllFieldConfigurationSchemes(String cloudId, ServiceCredentialsDto credentialsDto) {
        GetAllFieldConfigurationSchemes rs;
        rs = jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/fieldconfigurationscheme")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GetAllFieldConfigurationSchemes>() {
                })
                .block();
        return rs;
    }

    private FieldConfigurationScheme sendCreateFieldConfigurationScheme(CreateFieldConfigurationScheme fieldConfigurationScheme,
                                                                        String cloudId, ServiceCredentialsDto credentialsDto) {
        FieldConfigurationScheme rs;
        rs = jiraCloudApiClient.post()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/fieldconfigurationscheme")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .body(BodyInserters.fromValue(fieldConfigurationScheme))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<FieldConfigurationScheme>() {
                })
                .block();
        return rs;
    }


    private void sendAssignFieldConfigurationScheme(AssignFieldConfigurationScheme assignFieldConfigurationScheme,
                                                    String cloudId, ServiceCredentialsDto credentialsDto) {
        jiraCloudApiClient.put()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/fieldconfigurationscheme/project")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .body(BodyInserters.fromValue(assignFieldConfigurationScheme))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Void>() {
                })
                .block();
    }

    private GetFieldConfigurationIssueTypeItems sendGetFieldConfigurationIssueTypeItems(String cloudId, ServiceCredentialsDto credentialsDto) {
        return jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/fieldconfigurationscheme/mapping")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GetFieldConfigurationIssueTypeItems>() {
                })
                .block();
    }


    private void sendAssignIssueTypesToFieldConfigurations(AssignIssueTypesToFieldConfigurations assignIssueTypesToFieldConfigurations,
                                                           String configurationSchemaId,
                                                           String cloudId, ServiceCredentialsDto credentialsDto) {
        jiraCloudApiClient.put()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/fieldconfigurationscheme/" + configurationSchemaId + "/mapping")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .body(BodyInserters.fromValue(assignIssueTypesToFieldConfigurations))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Void>() {
                })
                .block();
    }


    private void sendEditIssue(String body, String issueKey, String cloudId, ServiceCredentialsDto credentialsDto) {
        jiraCloudApiClient.put()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/issue/" + issueKey)
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Void>() {
                })
                .block();
    }


    private GetTransitions sendGetTransitions(String issueKey, String cloudId, ServiceCredentialsDto credentialsDto) {
        return jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/issue/" + issueKey + "/transitions")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GetTransitions>() {
                })
                .block();
    }

    private void sendTransitionIssue(TransitionIssue transitionIssue, String issueKey, String cloudId, ServiceCredentialsDto credentialsDto) {
        jiraCloudApiClient.post()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/issue/" + issueKey + "/transitions")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .body(BodyInserters.fromValue(transitionIssue))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Void>() {
                })
                .block();
    }

    private FindUsersAndGroups sendFindUsersAndGroup(String query, String cloudId, ServiceCredentialsDto credentialsDto) {
        return jiraCloudApiClient.get()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/groupuserpicker?query=" + query)
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<FindUsersAndGroups>() {
                })
                .block();
    }

    private void sendAssignIssue(AssignIssue assignIssue, String issueKey, String cloudId, ServiceCredentialsDto credentialsDto) {
        jiraCloudApiClient.put()
                .uri("/ex/jira/" + cloudId + "/rest/api/3/issue/" + issueKey + "/assignee")
                .headers(headers -> headers.setBearerAuth(credentialsDto.getAccessToken()))
                .body(BodyInserters.fromValue(assignIssue))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Void>() {
                })
                .block();
    }


    @Override
    public void exportIssue(String connectingId, ExportIssueDto exportIssueDto) {
        ServiceCredentialsDto credentialsDto = getCredentialsWithoutOwner(connectingId);
        JiraAvailableResourceEntity resource = getJiraResource(connectingId);
        String cloudId = resource.getResourceId();
        credentialsDto = updateCredentialsWithoutOwner(credentialsDto);


        for (ExportIssueDto.IssueChangesDto issue : exportIssueDto.getIssues()) {

            GetTransitions transitions = sendGetTransitions(issue.getIssueId(), cloudId, credentialsDto);
            GetTransitions.Transition transition = transitions.getTransitions().stream()
                    .filter(trans -> trans.getTo().getId().equalsIgnoreCase(issue.getStatusId()))
                    .findFirst()
                    .orElse(null);
            if (transition != null) {
                TransitionIssue transitionIssue = TransitionIssue.builder()
                        .transition(TransitionIssue.Transition.builder()
                                            .id(transition.getId())
                                            .build())
                        .build();
                sendTransitionIssue(transitionIssue, issue.getIssueId(), cloudId, credentialsDto);
                log.info("ISSUE: {}, TRANSITIONED TO: {}", issue.getIssueId(), transition.getName());
            }

            String assigneeQuery = issue.getAssigneeEmail();
            FindUsersAndGroups usersAndGroups = sendFindUsersAndGroup(assigneeQuery, cloudId, credentialsDto);
            FindUsersAndGroups.User user = usersAndGroups.getUsers().getUsers().stream().findFirst().orElse(null);
            if (user != null) {
                AssignIssue assignIssue = AssignIssue.builder()
                        .accountId(user.getAccountId())
                        .build();
                sendAssignIssue(assignIssue, issue.getIssueId(), cloudId, credentialsDto);
                log.info("ISSUE: {}, ASSIGNED TO: {}", issue.getIssueId(), user.getDisplayName());
            }
        }
    }

    public static String createEditIssuePayload(String fieldName, Integer value) {
        try {
            Map<String, Object> fields = new HashMap<>();
            fields.put(fieldName, value);
            Map<String, Object> payload = new HashMap<>();
            payload.put("fields", fields);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private GetField createCustomEstimateFieldIfNotExists(String cloudId, ServiceCredentialsDto credentialsDto, String issueProjectId, GetIssue jiraIssue) {
        GetField field;
        CreateField createField = CreateField.builder()
                .name(CustomFieldConfig.SCRUMLY_ESTIMATE.getName())
                .type(CustomFieldConfig.SCRUMLY_ESTIMATE.getType())
                .build();
        field = sendCreateField(createField, cloudId, credentialsDto);

        FieldConfiguration fieldConfiguration = null;
        FieldConfigurationScheme fieldConfigurationScheme = null;
        try {

            // GET FIELD CONFIGURATION
            String configurationName = CustomFieldConfig.SCRUMLY_ESTIMATE.getName() + " Configuration";
            GetAllFieldsConfigurations fieldsConfigurations = sendGetAllFieldsConfigurations(cloudId, credentialsDto);
            fieldConfiguration = fieldsConfigurations.getValues().stream()
                    .filter(configuration -> configuration.getName().equalsIgnoreCase(configurationName))
                    .findAny()
                    .orElse(null);
            if (fieldConfiguration == null) {
                CreateFieldConfiguration createFieldConfiguration = CreateFieldConfiguration.builder()
                        .name(CustomFieldConfig.SCRUMLY_ESTIMATE.getName() + " Configuration")
                        .build();
                fieldConfiguration = sendCreateFieldConfiguration(createFieldConfiguration, cloudId, credentialsDto);
            }

            // GET FIELD CONFIGURATION ITEMS
            GetFieldConfigurationItems fieldConfigurationItems = sendGetAllFieldsConfigurations(cloudId, fieldConfiguration.getId(), credentialsDto);
            GetField finalField = field;
            UpdateFieldConfigurationItems.FieldConfigurationItem item = fieldConfigurationItems.getValues().stream()
                    .filter(configurationItem -> configurationItem.getId().equals(finalField.getId()))
                    .findAny()
                    .orElse(null);
            if (item == null) {
                UpdateFieldConfigurationItems updateFieldConfigurationItems = UpdateFieldConfigurationItems.builder()
                        .fieldConfigurationItems(Arrays.asList(
                                UpdateFieldConfigurationItems.FieldConfigurationItem.builder()
                                        .id(field.getId())
                                        .build()
                        ))
                        .build();
                sendUpdateFieldConfigurationItems(updateFieldConfigurationItems, fieldConfiguration.getId(), cloudId, credentialsDto);
            }

            // GET FIELD CONFIGURATION SCHEMES
            String schemeName = CustomFieldConfig.SCRUMLY_ESTIMATE.getName() + " Configuration Scheme";
            GetAllFieldConfigurationSchemes fieldConfigurationSchemes = sendGetAllFieldConfigurationSchemes(cloudId, credentialsDto);
            fieldConfigurationScheme = fieldConfigurationSchemes.getValues().stream()
                    .filter(configurationScheme -> configurationScheme.getName().equalsIgnoreCase(schemeName))
                    .findAny()
                    .orElse(null);

            if (fieldConfigurationScheme == null) {
                CreateFieldConfigurationScheme createFieldConfigurationScheme = CreateFieldConfigurationScheme.builder()
                        .name(schemeName)
                        .build();
                fieldConfigurationScheme = sendCreateFieldConfigurationScheme(createFieldConfigurationScheme, cloudId, credentialsDto);

                AssignFieldConfigurationScheme assignFieldConfigurationScheme = AssignFieldConfigurationScheme.builder()
                        .fieldConfigurationSchemeId(fieldConfigurationScheme.getId())
                        .projectId(issueProjectId)
                        .build();
                sendAssignFieldConfigurationScheme(assignFieldConfigurationScheme, cloudId, credentialsDto);
            }

            // GET FIELD CONFIGURATION ISSUE TYPES SCHEMES
            GetFieldConfigurationIssueTypeItems getFieldConfigurationIssueTypeItems = sendGetFieldConfigurationIssueTypeItems(cloudId, credentialsDto);

            FieldConfiguration finalFieldConfiguration = fieldConfiguration;
            String issueTypeId = jiraIssue.getFields().getIssueType().getId();
            FieldConfigurationScheme finalFieldConfigurationScheme = fieldConfigurationScheme;
            GetFieldConfigurationIssueTypeItems.FieldConfigurationIssueTypeItem fieldConfigurationIssueTypeItem = getFieldConfigurationIssueTypeItems.getValues().stream()
                    .filter(config -> config.getFieldConfigurationId().equals(finalFieldConfiguration.getId()) &&
                            config.getIssueTypeId().equals(issueTypeId) &&
                            config.getFieldConfigurationSchemeId().equals(finalFieldConfigurationScheme.getId()))
                    .findAny()
                    .orElse(null);
            if (fieldConfigurationIssueTypeItem == null) {
                AssignIssueTypesToFieldConfigurations assignIssueTypesToFieldConfigurations = AssignIssueTypesToFieldConfigurations.builder()
                        .mappings(Arrays.asList(
                                AssignIssueTypesToFieldConfigurations.IssueTypeFieldMapping.builder()
                                        .fieldConfigurationId(fieldConfiguration.getId())
                                        .issueTypeId(jiraIssue.getFields().getIssueType().getId())
                                        .build()
                        ))
                        .build();
                sendAssignIssueTypesToFieldConfigurations(assignIssueTypesToFieldConfigurations, fieldConfigurationScheme.getId(), cloudId, credentialsDto);
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (fieldConfigurationScheme != null) {
                sendDeleteFieldConfigurationScheme(fieldConfigurationScheme.getId(), cloudId, credentialsDto);
            }
            if (fieldConfiguration != null) {
                sendDeleteFieldConfiguration(fieldConfiguration.getId(), cloudId, credentialsDto);
            }
            if (field != null) {
                sendDeleteField(field.getId(), cloudId, credentialsDto);
            }
            field = null;
        }
        return field;
    }

    @Override
    public List<GetIssue> getIssues(String connectingId, List<String> keys) {
        return keys.stream().map(key -> getIssue(connectingId, key)).toList();
    }


    private JiraAvailableResourceEntity getJiraResource(String connectingId) {
        return jiraConnectionResourceService.getConnectionSelectedResource(connectingId);
    }

    private ServiceCredentialsDto getCredentials(String connectingId) {
        return credentialsService.findCredentials(connectingId, ServiceType.JIRA_CLOUD);
    }

    private ServiceCredentialsDto getCredentialsWithoutOwner(String connectingId) {
        return credentialsService.findCredentialsWithoutOwner(connectingId, ServiceType.JIRA_CLOUD);
    }

    private ServiceCredentialsDto updateCredentials(ServiceCredentialsDto credentialsDto) {
        credentialsDto = jiraCloudAuthService.refreshToken(credentialsDto);
        credentialsService.updateCredentials(credentialsDto);
        return credentialsDto;
    }

    private ServiceCredentialsDto updateCredentialsWithoutOwner(ServiceCredentialsDto credentialsDto) {
        credentialsDto = jiraCloudAuthService.refreshToken(credentialsDto);
        credentialsService.updateCredentialsWithoutOwner(credentialsDto);
        return credentialsDto;
    }
}
