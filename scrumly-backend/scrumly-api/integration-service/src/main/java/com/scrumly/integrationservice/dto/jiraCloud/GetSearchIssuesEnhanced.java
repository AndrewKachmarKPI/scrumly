package com.scrumly.integrationservice.dto.jiraCloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetSearchIssuesEnhanced {
    private List<SearchIssue> issues;

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchIssue {
        @JsonProperty("id")
        private String id;
        @JsonProperty("key")
        private String key;
        private String url;
        private String self;
        private Fields fields;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fields {
        private String summary;
        private Project project;
        private Status status;
        @JsonProperty("issuetype")
        private IssueType issueType;


        @Builder(toBuilder = true)
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Project {
            @JsonProperty("id")
            private String id;
            private String name;
        }

        @Builder(toBuilder = true)
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Status {
            @JsonProperty("id")
            private String id;
            private String name;
        }

        @Builder(toBuilder = true)
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class IssueType {
            @JsonProperty("id")
            private String id;
            private String iconUrl;
        }
    }
}
