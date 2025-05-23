package com.scrumly.eventservice.dto.requests.estimate;

import com.scrumly.eventservice.dto.requests.CreateActivityBlockRQ;
import com.scrumly.eventservice.enums.EstimationMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateEstimateBlockRQ extends CreateActivityBlockRQ {
    private EstimationMethod estimateMethod;
    private CreateEstimateScaleRQ createScaleRQ;
}
