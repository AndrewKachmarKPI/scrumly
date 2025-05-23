package com.scrumly.eventservice.dto.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.scrumly.eventservice.dto.requests.estimate.CreateEstimateBlockRQ;
import com.scrumly.eventservice.dto.requests.estimate.CreateEstimateScaleRQ;
import com.scrumly.eventservice.dto.requests.itemsBoard.CreateItemsBoardRQ;
import com.scrumly.eventservice.dto.requests.question.CreateQuestionBlockRQ;
import com.scrumly.eventservice.dto.requests.reflect.CreateReflectBlockRQ;
import com.scrumly.eventservice.enums.ActivityBlockType;
import com.scrumly.eventservice.services.factory.CreateBlockRQ;
import lombok.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, visible = true, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateQuestionBlockRQ.class, name = "QUESTION_BLOCK"),
        @JsonSubTypes.Type(value = CreateReflectBlockRQ.class, name = "REFLECT_BLOCK"),
        @JsonSubTypes.Type(value = CreateEstimateBlockRQ.class, name = "ESTIMATE_BLOCK"),
        @JsonSubTypes.Type(value = CreateItemsBoardRQ.class, name = "ITEM_BOARD_BLOCK")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateActivityBlockRQ implements CreateBlockRQ {
    private String name;
    private String description;
    private ActivityBlockType type;
    private Boolean isMandatory;
}
