package com.scrumly.integrationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class JiraCloudClientsConfigurations {
    @Value("${integration.jira-cloud.auth-url}")
    private String jiraCloudAuthUrl;
    @Value("${integration.jira-cloud.api-url}")
    private String jiraCloudApiUrl;

    @Bean
    public WebClient jiraCloudAuthClient() {
        return WebClient.builder()
                .baseUrl(jiraCloudAuthUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient jiraCloudApiClient() {
        return WebClient.builder()
                .baseUrl(jiraCloudApiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
