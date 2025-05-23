package com.scrumly.service.prompts;

import com.scrumly.integration.googleGemini.GeminiApiRequest;

public interface GenerateGeminiPromptService {
    <T> GeminiApiRequest getRequest(T args, Double temperature);
}
