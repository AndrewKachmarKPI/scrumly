package com.scrumly.integrationservice.service.google;

import com.scrumly.integrationservice.dto.googleGemini.GeminiApiRequest;
import com.scrumly.integrationservice.dto.googleGemini.GeminiApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class GoogleGeminiAIServiceImpl implements GoogleGeminiAIService {
    private final WebClient geminiClient;

    @Value("${integration.gemini.api-key}")
    private String apiKey;
    @Value("${integration.gemini.model}")
    private String model;

    @Override
    public GeminiApiResponse generateContent(GeminiApiRequest apiRequest) {
        if (apiRequest.getModel() == null) {
            apiRequest.setModel(model);
        }
        return geminiClient.post()
                .uri(":generateContent?key=" + apiKey)
                .body(BodyInserters.fromValue(apiRequest))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<GeminiApiResponse>() {
                })
                .block();
    }
}
