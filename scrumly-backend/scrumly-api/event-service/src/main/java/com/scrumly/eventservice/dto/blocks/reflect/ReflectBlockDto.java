package com.scrumly.eventservice.dto.blocks.reflect;

import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.enums.ActivityBlockType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ReflectBlockDto extends ActivityBlockDto {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ReflectColumnDto> reflectColumns;
    @Column(nullable = false)
    private Integer maxReflectionsPerColumnPerUser;
    @Column(nullable = false)
    private Integer timePerColumn;
    @Column
    private Long reflectTimeLimit;

    @Builder
    public ReflectBlockDto(Long id, String blockId, ActivityBlockType type, String name, String description, Boolean isMandatory, List<ReflectColumnDto> reflectColumns, Integer maxReflectionsPerColumnPerUser, Integer timePerColumn, Long reflectTimeLimit) {
        super(id, blockId, type, name, description, isMandatory);
        this.reflectColumns = reflectColumns;
        this.maxReflectionsPerColumnPerUser = maxReflectionsPerColumnPerUser;
        this.timePerColumn = timePerColumn;
        this.reflectTimeLimit = reflectTimeLimit;
    }
}
