package com.scrumly.eventservice.dto;

import com.scrumly.eventservice.enums.ActivityScope;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityOwnerDto {
    private Long id;
    private String createdById;
    private String ownerId;
    private LocalDateTime dateTimeCreated;
    private ActivityScope scope;
}
