package com.scrumly.integrationservice.dto.googleGemini;

import lombok.Data;

import java.util.List;

@Data
public class GeminiApiResponse {
    private List<Candidate> candidates;
    private UsageMetadata usageMetadata;
    private String modelVersion;

    @Data
    public static class Candidate {
        private Content content;
        private String finishReason;
        private CitationMetadata citationMetadata;
        private Double avgLogprobs;
    }

    @Data
    public static class Content {
        private List<Part> parts;
        private String role;
    }

    @Data
    public static class Part {
        private String text;
    }

    @Data
    public static class CitationMetadata {
        private List<CitationSource> citationSources;
    }

    @Data
    public static class CitationSource {
        private int startIndex;
        private int endIndex;
        private String uri;
    }

    @Data
    public static class UsageMetadata {
        private int promptTokenCount;
        private int candidatesTokenCount;
        private int totalTokenCount;
        private List<TokenDetail> promptTokensDetails;
        private List<TokenDetail> candidatesTokensDetails;
    }

    @Data
    public static class TokenDetail {
        private String modality;
        private int tokenCount;
    }
}
