package com.scrumly.eventservice.dto;

import com.scrumly.eventservice.dto.requests.CreateActivityBlockConfigRQ;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityTemplateDto {
    private String templateId;
    private String name;
    private String description;
    private String previewImageId;
    private List<String> tags;
    private ActivityTypeDto type;
    private ActivityOwnerDto owner;
    private List<ActivityBlockConfigDto> blocks;
    private List<CreateActivityBlockConfigRQ> newBlocks;
}
