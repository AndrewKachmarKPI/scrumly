package com.scrumly.eventservice.dto;

import com.scrumly.eventservice.enums.ActivityBlockType;
import lombok.*;

@ToString
@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityBlockConfigDto {
    private Long id;
    private Integer blockOrder;
    private String blockId;
    private ActivityBlockType blockType;
    private ActivityBlockDto blockDto;
}
