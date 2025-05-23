package com.scrumly.eventservice.dto.blocks.estimate;

import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.enums.ActivityBlockType;
import com.scrumly.eventservice.enums.EstimationMethod;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class EstimateBlockDto extends ActivityBlockDto {
    private EstimationMethod estimateMethod;
    private EstimateScaleDto scale;

    public EstimateBlockDto(EstimationMethod estimateMethod, EstimateScaleDto scale) {
        this.estimateMethod = estimateMethod;
        this.scale = scale;
    }

    @Builder
    public EstimateBlockDto(Long id, String blockId, ActivityBlockType type, String name, String description, Boolean isMandatory, EstimationMethod estimateMethod, EstimateScaleDto scale) {
        super(id, blockId, type, name, description, isMandatory);
        this.estimateMethod = estimateMethod;
        this.scale = scale;
    }
}
