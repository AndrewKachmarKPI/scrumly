package com.scrumly.eventservice.dto.requests.itemsBoard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateItemsBoardColumnRQ {
    private Long id;
    private Integer columnOrder;
    private String title;
    private String instruction;
    private String color;
    private Integer maxItems;
    private List<CreateItemsBoardColumnStatusRQ> statusMapping;
}
