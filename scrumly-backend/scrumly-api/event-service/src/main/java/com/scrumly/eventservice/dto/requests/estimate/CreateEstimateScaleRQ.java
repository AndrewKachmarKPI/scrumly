package com.scrumly.eventservice.dto.requests.estimate;

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
public class CreateEstimateScaleRQ {
    private Long id;
    private String name;
    private List<String> scale;
}
