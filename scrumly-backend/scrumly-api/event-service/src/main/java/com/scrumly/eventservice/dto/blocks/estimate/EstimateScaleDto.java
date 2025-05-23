package com.scrumly.eventservice.dto.blocks.estimate;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EstimateScaleDto {
    private Long id;
    private String name;
    private List<String> scale;
}
