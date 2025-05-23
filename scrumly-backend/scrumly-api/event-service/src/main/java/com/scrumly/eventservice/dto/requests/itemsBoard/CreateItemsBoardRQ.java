package com.scrumly.eventservice.dto.requests.itemsBoard;

import com.scrumly.eventservice.dto.requests.CreateActivityBlockRQ;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateItemsBoardRQ extends CreateActivityBlockRQ {
    private List<CreateItemsBoardColumnRQ> boardColumns;
    private Integer maxItems;
    private List<CreateItemsBoardColumnStatusRQ> doneStatuses;
    private List<CreateItemsBoardColumnStatusRQ> inProgressStatuses;
}
