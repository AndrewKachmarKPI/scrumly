package com.scrumly.eventservice.domain.blocks.estimate;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.enums.ActivityBlockType;
import com.scrumly.eventservice.enums.EstimationMethod;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
@Entity
public class EstimateBlockEntity extends ActivityBlockEntity {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstimationMethod estimateMethod;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private EstimateScaleEntity scale;

    public EstimateBlockEntity(EstimateBlockEntity block) {
        super(block);
        this.estimateMethod = block.getEstimateMethod();
        this.scale = EstimateScaleEntity.builder()
                .name(block.getScale().getName())
                .scale(new ArrayList<>(block.getScale().getScale()))
                .build();
    }

    @Builder
    public EstimateBlockEntity(Long id, String blockId, ActivityBlockType type, String name, String description,
                               Boolean isMandatory, EstimationMethod estimateMethod, EstimateScaleEntity scale) {
        super(id, blockId, type, name, description, isMandatory);
        this.estimateMethod = estimateMethod;
        this.scale = scale;
    }

    public EstimateBlockEntity(ActivityBlockEntity block, EstimationMethod estimateMethod, EstimateScaleEntity scale) {
        super(block);
        this.estimateMethod = estimateMethod;
        this.scale = scale;
    }
}
