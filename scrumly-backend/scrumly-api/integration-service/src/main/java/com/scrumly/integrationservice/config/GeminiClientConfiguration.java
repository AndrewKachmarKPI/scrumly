package com.scrumly.integrationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiClientConfiguration {
    @Value("${integration.gemini.api-url}")
    private String apiUrl;
    @Value("${integration.gemini.model}")
    private String model;

    @Bean
    public WebClient geminiClient() {
        return WebClient.builder()
                .baseUrl(apiUrl + "/" + model)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
