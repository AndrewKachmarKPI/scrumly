package com.scrumly.eventservice.dto.requests;

import com.scrumly.eventservice.enums.ActivityBlockType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateActivityBlockConfigRQ {
    private Integer order;
    private Boolean isOptional;
    private CreateActivityBlockRQ block;
}
