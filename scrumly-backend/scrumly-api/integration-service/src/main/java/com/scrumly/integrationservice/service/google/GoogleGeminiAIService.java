package com.scrumly.integrationservice.service.google;

import com.scrumly.integrationservice.dto.googleGemini.GeminiApiRequest;
import com.scrumly.integrationservice.dto.googleGemini.GeminiApiResponse;

public interface GoogleGeminiAIService {
    GeminiApiResponse generateContent(GeminiApiRequest apiRequest);
}
