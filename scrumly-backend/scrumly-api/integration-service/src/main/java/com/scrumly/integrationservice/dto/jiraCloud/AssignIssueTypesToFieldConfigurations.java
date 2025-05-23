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
public class AssignIssueTypesToFieldConfigurations {
    private List<IssueTypeFieldMapping> mappings;

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IssueTypeFieldMapping {
        private String fieldConfigurationId;
        private String issueTypeId;
    }
}
