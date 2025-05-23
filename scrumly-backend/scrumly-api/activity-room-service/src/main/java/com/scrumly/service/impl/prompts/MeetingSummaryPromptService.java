package com.scrumly.service.impl.prompts;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.service.prompts.GeminiPromptService;
import org.springframework.stereotype.Service;

import static com.scrumly.mappers.BusinessMapper.serializeMeetingRoomWithoutIds;

public class MeetingSummaryPromptService implements GeminiPromptService {
    @Override
    public <T> String getPrompt(T args) {
        if (args instanceof ActivityRoom room) {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Analyze the following JSON representing a Scrum meeting and generate an HTML summary as per the system instruction.\n");
            prompt.append("Omit any blocks that contain no user responses.\n");
            prompt.append("======== SCRUM MEETING DATA ========\n");
            prompt.append(serializeMeetingRoomWithoutIds(room)).append("\n");
            prompt.append("====================================");
            return prompt.toString();
        } else {
            throw new ServiceErrorException("Could not create prompt");
        }
    }

    @Override
    public <T> String getSystemInstruction(T args) {
        return """
                You are an assistant that summarizes Agile Scrum meeting collaborative workspace into well-structured HTML that will be displayed in a Quill rich text editor.
                            
                Take the following raw meeting transcript and return a clean, styled summary in HTML format with the following requirements:
                            
                1. Use standard HTML tags like <h2>, <ul>, <li>, <strong>, <em>, and <p> that are compatible with the Quill editor.
                2. Add relevant emojis to enhance readability and engagement, not so many.
                3. Mandatory sections:
                   - Title and date
                   - Participants
                   - Summary/analysis of how the meeting went
                   - Key discussion points, information about meeting blocks, including boards info, retrospective or any other
                   info available
                4. Keep formatting clean and visually pleasant.
                5. Do not wrap the HTML in <html> or <body> tags – just the inner HTML that Quill can accept.
                6. Make the document look pretty, add spacing so that content looks good.
                """;
    }
}
