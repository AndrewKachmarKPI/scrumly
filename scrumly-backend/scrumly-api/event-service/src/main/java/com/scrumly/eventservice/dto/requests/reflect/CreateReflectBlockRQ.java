package com.scrumly.eventservice.dto.requests.reflect;

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
public class CreateReflectBlockRQ extends CreateActivityBlockRQ {
    private List<CreateReflectColumnRQ> reflectColumns;
    private Integer maxReflectionsPerColumnPerUser;
    private Integer timePerColumn;
    private Long reflectTimeLimit;
}
