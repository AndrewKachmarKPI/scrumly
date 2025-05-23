package com.scrumly.dto.prompts;

import com.scrumly.domain.ActivityRoom;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GenerateReflectionRQ {
    private ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnMetadata columnMetadata;
    private ActivityRoom.ActivityBlock.ActivityReflectBlock.ReflectColumnCard columnCard;
    private String prompt;
}
