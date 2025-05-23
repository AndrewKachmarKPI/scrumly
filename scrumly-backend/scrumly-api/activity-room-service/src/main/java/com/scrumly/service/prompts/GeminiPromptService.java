package com.scrumly.service.prompts;


public interface GeminiPromptService {
    <T> String getPrompt(T args);

    <T> String getSystemInstruction(T args);
}
