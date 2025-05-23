package com.scrumly.eventservice.dto.blocks.itemsBoard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ItemsBoardColumnDto {
    private Long id;
    private Integer columnOrder;
    private String title;
    private String color;
    private String instruction;
    private Integer maxItems;
    private List<ItemsBoardColumnStatusDto> statusMapping;
}
