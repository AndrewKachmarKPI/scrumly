package com.scrumly.eventservice.dto.requests.itemsBoard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateItemsBoardColumnStatusRQ {
    private Long id;
    private String statusId;
    private String backlogId;
    private String status;
    private String color;
}
