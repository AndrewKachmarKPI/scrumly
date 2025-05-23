package com.scrumly.eventservice.dto.blocks.question;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.dto.requests.question.CreateQuestionBlockRQ;
import com.scrumly.eventservice.dto.requests.reflect.CreateReflectBlockRQ;
import com.scrumly.eventservice.enums.ActivityBlockType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class QuestionBlockDto extends ActivityBlockDto {
    private List<QuestionDto> questions;
    private Long answerTimeLimit;

    @Builder
    public QuestionBlockDto(Long id, String blockId, ActivityBlockType type, String name, String description, Boolean isMandatory, List<QuestionDto> questions, Long answerTimeLimit) {
        super(id, blockId, type, name, description, isMandatory);
        this.questions = questions;
        this.answerTimeLimit = answerTimeLimit;
    }
}
