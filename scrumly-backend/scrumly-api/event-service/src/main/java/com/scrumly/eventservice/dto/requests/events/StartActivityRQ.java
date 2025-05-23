package com.scrumly.eventservice.dto.requests.events;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class StartActivityRQ {
    private String activityId;
    private String teamId;
    private String templateId;
}
