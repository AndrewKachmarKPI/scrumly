package com.scrumly.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class SyncBlockOption {
    private String activityId;
    private String teamId;
    private String eventTitle;
    private String eventStartDateTime;
    private String eventEndDateTime;
    private String templateName;
    private String templateId;
}
