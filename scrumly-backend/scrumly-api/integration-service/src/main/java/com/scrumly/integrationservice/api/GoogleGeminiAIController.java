package com.scrumly.integrationservice.api;

import com.scrumly.integrationservice.dto.googleGemini.GeminiApiRequest;
import com.scrumly.integrationservice.dto.googleGemini.GeminiApiResponse;
import com.scrumly.integrationservice.service.google.GoogleGeminiAIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/google/gemini")
@RequiredArgsConstructor
@Validated
public class GoogleGeminiAIController {
    private final GoogleGeminiAIService googleGeminiAIService;

    @PostMapping("/generateContent")
    public ResponseEntity<GeminiApiResponse> generateContent(@Valid @RequestBody GeminiApiRequest apiRequest) {
        return ResponseEntity.ok(googleGeminiAIService.generateContent(apiRequest));
    }
}
