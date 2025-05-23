package com.scrumly.integrationservice.dto.jiraCloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GetIssue {

    @JsonProperty("fields")
    private Fields fields;
    private RenderedFields renderedFields;
    private String id;
    private String key;
    private String self;
    private String expand;

    private InternalFields internalFields;

    @Data
    @NoArgsConstructor
    public static class RenderedFields {
        private String lastViewed;
        private String created;
        private String updated;
        private String description;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Fields {
        private String summary;
        @JsonProperty("issuetype")
        private IssueType issueType;
        @JsonProperty("project")
        private Project project;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InternalFields {
        private String url;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IssueType {
        private String id;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Project {
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class TimeTracking {
        private String originalEstimate;
        private long originalEstimateSeconds;
        private String remainingEstimate;
        private long remainingEstimateSeconds;
        private String timeSpent;
        private long timeSpentSeconds;
    }
}
