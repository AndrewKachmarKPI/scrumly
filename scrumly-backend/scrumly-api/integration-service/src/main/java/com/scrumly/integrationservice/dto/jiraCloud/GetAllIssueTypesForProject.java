package com.scrumly.integrationservice.dto.jiraCloud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GetAllIssueTypesForProject {
    private int avatarId;
    private String description;
    private String entityId;
    private int hierarchyLevel;
    private String iconUrl;
    private String id;
    private String name;
    private Scope scope;
    private String self;
    private boolean subtask;

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Scope {
        private Project project;
        private String type;
    }

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Project {
        private String id;
    }
}
