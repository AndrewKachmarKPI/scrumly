package com.scrumly.eventservice.dto.requests;

import com.scrumly.eventservice.enums.ActivityScope;
import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateActivityTemplateRQ {
    private String name;
    private String description;
    private String previewImageId;
    private List<String> tags;
    private String activityType;
    private String ownerId;
    private ActivityScope scope;
    private List<CreateActivityBlockConfigRQ> blocks;
}
