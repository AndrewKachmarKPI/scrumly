package com.scrumly.integrationservice.dto.jiraCloud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreateIssue {
    private CreateIssueFields fields;

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateIssueFields {
        private DescriptionField description;
        private String summary;
        private ProjectField project;
        private IssueTypeField issuetype;
        @Data
        @Builder(toBuilder = true)
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ProjectField {
            private String id;
        }

        @Data
        @Builder(toBuilder = true)
        @AllArgsConstructor
        @NoArgsConstructor
        public static class IssueTypeField {
            private String id;
        }

        @Data
        @Builder(toBuilder = true)
        @AllArgsConstructor
        @NoArgsConstructor
        public static class DescriptionField {
            private List<ContentWrapper> content;
            private String type;
            private int version;

            @Data
            @Builder(toBuilder = true)
            @AllArgsConstructor
            @NoArgsConstructor
            public static class ContentWrapper {
                private List<ContentItem> content;
                private String type;
            }

            @Data
            @Builder(toBuilder = true)
            @AllArgsConstructor
            @NoArgsConstructor
            public static class ContentItem {
                private String text;
                private String type;
            }
        }
    }
}
