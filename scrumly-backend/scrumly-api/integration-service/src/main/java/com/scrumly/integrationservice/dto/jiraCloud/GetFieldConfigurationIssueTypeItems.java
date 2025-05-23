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
public class GetFieldConfigurationIssueTypeItems {
    private List<FieldConfigurationIssueTypeItem> values;

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldConfigurationIssueTypeItem {
        private String fieldConfigurationSchemeId;
        private String issueTypeId;
        private String fieldConfigurationId;
    }

}
