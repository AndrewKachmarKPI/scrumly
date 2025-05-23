package com.scrumly.eventservice.dto.blocks.itemsBoard;

import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.enums.ActivityBlockType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ItemsBoardBlockDto extends ActivityBlockDto {
    private List<ItemsBoardColumnDto> boardColumns;
    private Integer maxItems;
    private List<ItemsBoardColumnStatusDto> doneStatuses;
    private List<ItemsBoardColumnStatusDto> inProgressStatuses;

    @Builder
    public ItemsBoardBlockDto(Long id, String blockId, ActivityBlockType type, String name, String description, Boolean isMandatory, List<ItemsBoardColumnDto> boardColumns, Integer maxItems, List<ItemsBoardColumnStatusDto> doneStatuses, List<ItemsBoardColumnStatusDto> inProgressStatuses) {
        super(id, blockId, type, name, description, isMandatory);
        this.boardColumns = boardColumns;
        this.maxItems = maxItems;
        this.doneStatuses = doneStatuses;
        this.inProgressStatuses = inProgressStatuses;
    }
}
