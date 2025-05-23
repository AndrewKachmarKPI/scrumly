package com.scrumly.eventservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityTemplateGroupDto {
    private String group;
    private List<ActivityTemplateDto> templates;
}
