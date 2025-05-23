package com.scrumly.eventservice.dto.requests.question;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateQuestionRQ {
    private String question;
    private List<String> answerOptions;
}
