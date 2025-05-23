package com.scrumly.eventservice.services;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.dto.requests.CreateActivityBlockRQ;
import com.scrumly.eventservice.enums.ActivityBlockType;

import java.util.List;

public interface ActivityBlockService {
    ActivityBlockEntity createActivityBlock(CreateActivityBlockRQ createActivityBlockRQ);

    ActivityBlockEntity updateActivityBlock(String blockId, ActivityBlockDto blockDto);

    ActivityBlockEntity findActivityBlock(String blockId, ActivityBlockType blockType);

    ActivityBlockEntity copyActivityBlock(String blockId, ActivityBlockType blockType);
    void deleteActivityBlock(String blockId, ActivityBlockType blockType);
}
