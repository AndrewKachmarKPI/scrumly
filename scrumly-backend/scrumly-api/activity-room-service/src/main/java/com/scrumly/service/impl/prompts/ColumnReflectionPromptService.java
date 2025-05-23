package com.scrumly.service.impl.prompts;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.dto.prompts.GenerateReflectionRQ;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.service.prompts.GeminiPromptService;

import java.util.stream.Collectors;

public class ColumnReflectionPromptService implements GeminiPromptService {
    @Override
    public <T> String getPrompt(T args) {
        if (args instanceof GenerateReflectionRQ reflectionRQ) {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Generate one short, clear reflection for the retrospective board column.\n");

            //FIXME If column is not initialized add check
            if (reflectionRQ.getColumnCard() != null) {
                prompt.append("Column title: ").append(reflectionRQ.getColumnCard().getColumnMetadata().getTitle()).append("\n");
                prompt.append("Column instruction: ").append(reflectionRQ.getColumnCard().getColumnMetadata().getInstruction()).append("\n");
                prompt.append("Existing reflections: ").append("[")
                        .append(reflectionRQ.getColumnCard().getUserColumnReflectCards().stream()
                                        .map(ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard.UserColumnReflectCard::getCardContent)
                                        .collect(Collectors.joining(",")))
                        .append("]").append("\n");
            } else if (reflectionRQ.getColumnMetadata() != null) {
                prompt.append("Column title: ").append(reflectionRQ.getColumnMetadata().getTitle()).append("\n");
                prompt.append("Column instruction: ").append(reflectionRQ.getColumnMetadata().getInstruction()).append("\n");
            }
            if (reflectionRQ.getPrompt() != null) {
                prompt.append("User reflection prompt: ").append(reflectionRQ.getPrompt()).append("\n");
            }
            prompt.append("Your response must be a single line of plain text, without any formatting.");
            return prompt.toString();
        } else {
            throw new ServiceErrorException("Could not create prompt");
        }
    }

    @Override
    public <T> String getSystemInstruction(T args) {
        return "You are a team member participating in a retrospective. Write a brief, thoughtful reflection in plain text for the specified column. Do not include any formatting—just one line of text.";
    }
}
