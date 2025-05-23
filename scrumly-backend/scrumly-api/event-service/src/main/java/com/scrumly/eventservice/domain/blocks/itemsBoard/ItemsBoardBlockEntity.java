package com.scrumly.eventservice.domain.blocks.itemsBoard;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.enums.ActivityBlockType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
@Entity
public class ItemsBoardBlockEntity extends ActivityBlockEntity {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ItemsBoardColumnEntity> boardColumns;
    @Column
    private Integer maxItems;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ItemsBoardColumnStatusEntity> doneStatuses = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ItemsBoardColumnStatusEntity> inProgressStatuses = new ArrayList<>();

    @Builder
    public ItemsBoardBlockEntity(Long id, String blockId, ActivityBlockType type, String name, String description, Boolean isMandatory, List<ItemsBoardColumnEntity> boardColumns, Integer maxItems, List<ItemsBoardColumnStatusEntity> doneStatuses, List<ItemsBoardColumnStatusEntity> inProgressStatuses) {
        super(id, blockId, type, name, description, isMandatory);
        this.boardColumns = boardColumns;
        this.maxItems = maxItems;
        this.doneStatuses = doneStatuses;
        this.inProgressStatuses = inProgressStatuses;
    }

    public ItemsBoardBlockEntity(ItemsBoardBlockEntity block) {
        super(block);
        this.boardColumns = block.boardColumns != null
                ? block.boardColumns.stream().map(ItemsBoardColumnEntity::new).collect(Collectors.toList())
                : null;
        this.maxItems = block.maxItems;
        this.doneStatuses = block.doneStatuses != null
                ? block.doneStatuses.stream().map(ItemsBoardColumnStatusEntity::new).collect(Collectors.toList())
                : null;
        this.inProgressStatuses = block.inProgressStatuses != null
                ? block.inProgressStatuses.stream().map(ItemsBoardColumnStatusEntity::new).collect(Collectors.toList())
                : null;
    }

}
