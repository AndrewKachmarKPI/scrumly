package com.scrumly.integrationservice.enums.jira;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomFieldConfig {
    SCRUMLY_ESTIMATE(
            "Scrumly estimate",
            "com.atlassian.jira.plugin.system.customfieldtypes:textfield"
    );

    private final String name;
    private final String type;
}
