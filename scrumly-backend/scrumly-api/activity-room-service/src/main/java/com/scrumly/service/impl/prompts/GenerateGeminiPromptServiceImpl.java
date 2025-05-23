package com.scrumly.service.impl.prompts;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.dto.prompts.GenerateReflectionRQ;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.integration.googleGemini.GeminiApiRequest;
import com.scrumly.service.prompts.GeminiPromptService;
import com.scrumly.service.prompts.GenerateGeminiPromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class GenerateGeminiPromptServiceImpl implements GenerateGeminiPromptService {

    private static final Integer MAX_TOKENS = 2000;
    private static final Double TEMPERATURE = 0.5;


    @Override
    public <T> GeminiApiRequest getRequest(T args, Double temperature) {
        GeminiPromptService promptService = getPromptService(args);
        return GeminiApiRequest.builder()
                .contents(Arrays.asList(
                        GeminiApiRequest.Content.builder()
                                .parts(Arrays.asList(
                                        GeminiApiRequest.Part.builder()
                                                .text(promptService.getPrompt(args))
                                                .build()
                                ))
                                .build()
                ))
                .system_instruction(GeminiApiRequest.SystemInstruction.builder()
                                            .parts(Arrays.asList(
                                                    GeminiApiRequest.Part.builder()
                                                            .text(promptService.getSystemInstruction(args))
                                                            .build()
                                            ))
                                            .build())
                .generationConfig(GeminiApiRequest.GenerationConfig.builder()
                                          .maxOutputTokens(MAX_TOKENS)
                                          .temperature(temperature)
                                          .build())
                .build();
    }

    public <T> GeminiPromptService getPromptService(T args) {
        if (args instanceof ActivityRoom) {
            return new MeetingSummaryPromptService();
        } else if (args instanceof GenerateReflectionRQ) {
            return new ColumnReflectionPromptService();
        }
        throw new ServiceErrorException("Could not construct prompt service");
    }
}
