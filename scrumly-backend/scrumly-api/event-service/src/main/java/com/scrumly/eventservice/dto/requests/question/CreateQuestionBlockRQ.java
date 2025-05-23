package com.scrumly.eventservice.dto.requests.question;

import com.scrumly.eventservice.dto.requests.CreateActivityBlockRQ;
import com.scrumly.eventservice.enums.ActivityBlockType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateQuestionBlockRQ extends CreateActivityBlockRQ {
    @NotEmpty
    private List<@Valid CreateQuestionRQ> questions;
    private Long answerTimeLimit;

    @Builder
    public CreateQuestionBlockRQ(String name, String description, ActivityBlockType type, Boolean isMandatory, List<@Valid CreateQuestionRQ> questions, Long answerTimeLimit) {
        super(name, description, type, isMandatory);
        this.questions = questions;
        this.answerTimeLimit = answerTimeLimit;
    }

    public CreateQuestionBlockRQ(List<@Valid CreateQuestionRQ> questions, Long answerTimeLimit) {
        this.questions = questions;
        this.answerTimeLimit = answerTimeLimit;
    }
}
