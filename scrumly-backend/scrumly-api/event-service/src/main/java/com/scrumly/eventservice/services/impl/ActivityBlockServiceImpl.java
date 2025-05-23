package com.scrumly.eventservice.services.impl;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.dto.requests.CreateActivityBlockRQ;
import com.scrumly.eventservice.enums.ActivityBlockType;
import com.scrumly.eventservice.services.ActivityBlockService;
import com.scrumly.eventservice.services.factory.ActivityBlockFactory;
import com.scrumly.eventservice.services.factory.ActivityBlockFactoryProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityBlockServiceImpl implements ActivityBlockService {

    private final ActivityBlockFactoryProducer blockFactoryProducer;

    @Override
    public ActivityBlockEntity createActivityBlock(CreateActivityBlockRQ createActivityBlockRQ) {
        ActivityBlockType blockType = createActivityBlockRQ.getType();
        ActivityBlockFactory factory = getBlockFactory(blockType);
        return factory.createActivityBlock(createActivityBlockRQ);
    }

    @Override
    public ActivityBlockEntity updateActivityBlock(String blockId, ActivityBlockDto blockDto) {
        ActivityBlockType blockType = blockDto.getType();
        ActivityBlockFactory factory = getBlockFactory(blockType);
        return factory.updateActivityBlock(blockId, blockDto);
    }

    @Override
    public ActivityBlockEntity findActivityBlock(String blockId, ActivityBlockType blockType) {
        ActivityBlockFactory factory = getBlockFactory(blockType);
        return factory.findActivityBlock(blockId);
    }

    @Override
    public ActivityBlockEntity copyActivityBlock(String blockId, ActivityBlockType blockType) {
        ActivityBlockFactory factory = getBlockFactory(blockType);
        return factory.copyActivityBlock(blockId);
    }

    @Override
    public void deleteActivityBlock(String blockId, ActivityBlockType blockType) {
        ActivityBlockFactory factory = getBlockFactory(blockType);
        factory.deleteActivityBlock(blockId);
    }

    private ActivityBlockFactory getBlockFactory(ActivityBlockType blockType) {
        ActivityBlockFactory factory = blockFactoryProducer.getFactory(blockType);
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported ActivityBlockType: " + blockType);
        }
        return factory;
    }
}
