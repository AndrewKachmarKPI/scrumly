package com.scrumly.eventservice.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.scrumly.eventservice.dto.blocks.estimate.EstimateBlockDto;
import com.scrumly.eventservice.dto.blocks.itemsBoard.ItemsBoardBlockDto;
import com.scrumly.eventservice.dto.blocks.question.QuestionBlockDto;
import com.scrumly.eventservice.dto.blocks.reflect.ReflectBlockDto;
import com.scrumly.eventservice.enums.ActivityBlockType;
import lombok.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, visible = true, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = QuestionBlockDto.class, name = "QUESTION_BLOCK"),
        @JsonSubTypes.Type(value = ReflectBlockDto.class, name = "REFLECT_BLOCK"),
        @JsonSubTypes.Type(value = EstimateBlockDto.class, name = "ESTIMATE_BLOCK"),
        @JsonSubTypes.Type(value = ItemsBoardBlockDto.class, name = "ITEM_BOARD_BLOCK")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivityBlockDto {
    private Long id;
    private String blockId;
    private ActivityBlockType type;
    private String name;
    private String description;
    private Boolean isMandatory = Boolean.TRUE;
}
