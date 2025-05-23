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
public class GetIssuePickerSuggestions {

    @JsonProperty("sections")
    private List<Section> sections;

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Section {

        @JsonProperty("id")
        private String id;

        @JsonProperty("issues")
        private List<Issue> issues;

        @JsonProperty("label")
        private String label;

        @JsonProperty("msg")
        private String msg;

        @JsonProperty("sub")
        private String sub;
    }

    @Builder(toBuilder = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Issue {

        @JsonProperty("id")
        private int id;

        @JsonProperty("img")
        private String img;

        @JsonProperty("url")
        private String url;

        @JsonProperty("key")
        private String key;

        @JsonProperty("keyHtml")
        private String keyHtml;

        @JsonProperty("summary")
        private String summary;

        @JsonProperty("summaryText")
        private String summaryText;
    }
}