package com.scrumly.utils;

import com.scrumly.integration.googleGemini.GeminiApiResponse;

public class GeminiPromptUtils {
    public static String getFlatResponse(GeminiApiResponse response) {
        StringBuilder stringBuilder = new StringBuilder();
        for (GeminiApiResponse.Candidate candidate : response.getCandidates()) {
            for (GeminiApiResponse.Part part : candidate.getContent().getParts()) {
                stringBuilder.append(part.getText());
            }
        }
        return stringBuilder
                .toString()
                .replaceAll("html", "")
                .replaceAll("```", "");
    }
}
