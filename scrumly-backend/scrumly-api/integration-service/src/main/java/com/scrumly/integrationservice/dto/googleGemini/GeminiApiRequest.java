package com.scrumly.integrationservice.dto.googleGemini;

import lombok.Data;

import java.util.List;

@Data
public class GeminiApiRequest {
    private String model;
    private List<Content> contents;
    private SystemInstruction system_instruction;
    private GenerationConfig generationConfig;

    @Data
    public static class Content {
        private List<Part> parts;
    }

    @Data
    public static class Part {
        private String text;
    }

    @Data
    public static class SystemInstruction {
        private List<Part> parts;
    }

    @Data
    public static class GenerationConfig {
        private int maxOutputTokens;
        private double temperature;
    }
}
