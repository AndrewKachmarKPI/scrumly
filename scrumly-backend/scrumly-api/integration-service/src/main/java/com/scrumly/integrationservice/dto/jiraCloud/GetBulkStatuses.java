package com.scrumly.integrationservice.dto.jiraCloud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GetBulkStatuses {
    private String id;
    private String name;
    private Scope scope;

    @Data
    public static class Scope {
        private Project project;
        private String type;
    }

    @Data
    public static class Project {
        private String id;
    }
}
