package com.scrumly.eventservice.domain.blocks.reflect;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.enums.ActivityBlockType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class ReflectBlockEntity extends ActivityBlockEntity {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ReflectColumnEntity> reflectColumns;
    @Column
    private Integer maxReflectionsPerColumnPerUser;
    @Column
    private Integer timePerColumn;
    @Column
    private Long reflectTimeLimit;

    public ReflectBlockEntity(ReflectBlockEntity block) {
        super(block);
        this.reflectColumns = block.reflectColumns != null
                ? block.reflectColumns.stream().map(ReflectColumnEntity::new).collect(Collectors.toList())
                : null;
        this.maxReflectionsPerColumnPerUser = block.maxReflectionsPerColumnPerUser;
        this.timePerColumn = block.timePerColumn;
        this.reflectTimeLimit = block.reflectTimeLimit;
    }

    @Builder
    public ReflectBlockEntity(Long id, String blockId, ActivityBlockType type, String name, String description, Boolean isMandatory, List<ReflectColumnEntity> reflectColumns, Integer maxReflectionsPerColumnPerUser, Integer timePerColumn, Long reflectTimeLimit) {
        super(id, blockId, type, name, description, isMandatory);
        this.reflectColumns = reflectColumns;
        this.maxReflectionsPerColumnPerUser = maxReflectionsPerColumnPerUser;
        this.timePerColumn = timePerColumn;
        this.reflectTimeLimit = reflectTimeLimit;
    }

    @Builder
    public ReflectBlockEntity(ActivityBlockEntity block, List<ReflectColumnEntity> reflectColumns, Integer maxReflectionsPerColumnPerUser, Integer timePerColumn, Long reflectTimeLimit) {
        super(block);
        this.reflectColumns = reflectColumns;
        this.maxReflectionsPerColumnPerUser = maxReflectionsPerColumnPerUser;
        this.timePerColumn = timePerColumn;
        this.reflectTimeLimit = reflectTimeLimit;
    }
}
