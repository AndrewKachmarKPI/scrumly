package com.scrumly.eventservice.services.factory;

import com.scrumly.eventservice.enums.ActivityBlockType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityBlockFactoryProducer {
    private final QuestionBlockFactory questionBlockFactory;
    private final ReflectBlockFactory reflectBlockFactory;
    private final EstimateBlockFactory estimateBlockFactory;
    private final ItemsBoardBlockFactory itemsBoardBlockFactory;

    public ActivityBlockFactory getFactory(ActivityBlockType type) {
        return switch (type) {
            case QUESTION_BLOCK -> questionBlockFactory;
            case REFLECT_BLOCK -> reflectBlockFactory;
            case ESTIMATE_BLOCK -> estimateBlockFactory;
            case ITEM_BOARD_BLOCK -> itemsBoardBlockFactory;
            default -> throw new IllegalArgumentException("Unknown activity block type");
        };
    }
}
