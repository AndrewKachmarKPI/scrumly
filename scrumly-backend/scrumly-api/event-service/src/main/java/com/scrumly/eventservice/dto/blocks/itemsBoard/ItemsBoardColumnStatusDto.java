package com.scrumly.eventservice.dto.blocks.itemsBoard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public class ItemsBoardColumnStatusDto {
    private Long id;
    private String statusId;
    private String backlogId;
    private String status;
    private String color;
}
