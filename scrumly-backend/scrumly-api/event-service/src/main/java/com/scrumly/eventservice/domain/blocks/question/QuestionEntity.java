package com.scrumly.eventservice.domain.blocks.question;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
@Entity
public class QuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String question;
    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> answerOptions;

    public QuestionEntity(QuestionEntity questionEntity) {
        this.question = questionEntity.question;
        this.answerOptions = new ArrayList<>(questionEntity.answerOptions);
    }

}
