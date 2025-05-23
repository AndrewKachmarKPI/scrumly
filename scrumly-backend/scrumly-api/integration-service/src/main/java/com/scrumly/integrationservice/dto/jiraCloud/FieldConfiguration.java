package com.scrumly.integrationservice.dto.jiraCloud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FieldConfiguration {
    private String id;
    private String description;
    private String name;
}
