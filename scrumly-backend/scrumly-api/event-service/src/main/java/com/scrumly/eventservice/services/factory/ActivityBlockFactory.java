package com.scrumly.eventservice.services.factory;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.dto.requests.CreateActivityBlockRQ;

public interface ActivityBlockFactory {
    ActivityBlockEntity createActivityBlock(CreateActivityBlockRQ createRQ);
    ActivityBlockEntity updateActivityBlock(String blockId, ActivityBlockDto blockDto);
    void deleteActivityBlock(String blockId);
    ActivityBlockEntity findActivityBlock(String blockId);
    ActivityBlockEntity copyActivityBlock(String blockId);
}
