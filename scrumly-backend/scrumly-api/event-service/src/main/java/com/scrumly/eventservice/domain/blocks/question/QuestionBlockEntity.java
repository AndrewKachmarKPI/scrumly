package com.scrumly.eventservice.domain.blocks.question;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.enums.ActivityBlockType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
@Entity
public class QuestionBlockEntity extends ActivityBlockEntity {
    @Column(nullable = false)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<QuestionEntity> questions;
    @Column
    private Long answerTimeLimit;

    public QuestionBlockEntity(QuestionBlockEntity questionBlock) {
        super(questionBlock);
        this.questions = questionBlock.questions != null
                ? questionBlock.questions.stream().map(QuestionEntity::new).collect(Collectors.toList())
                : null;
        this.answerTimeLimit = questionBlock.answerTimeLimit;
    }

    @Builder
    public QuestionBlockEntity(Long id, String blockId, ActivityBlockType type, String name, String description, Boolean isMandatory, List<QuestionEntity> questions, Long answerTimeLimit) {
        super(id, blockId, type, name, description, isMandatory);
        this.questions = questions;
        this.answerTimeLimit = answerTimeLimit;
    }
}
