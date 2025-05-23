package com.scrumly.dto.backlog;

import com.scrumly.enums.integration.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class IssueExportOption {
    private ServiceType serviceType;
    private String projectName;
    private String projectId;
    private List<ProjectIssueType> issueTypes;
    private String organizationId;
    private String issueKey;
    private ProjectIssueType issueType;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @EqualsAndHashCode
    @Builder(toBuilder = true)
    public static class ProjectIssueType {
        private String id;
        private String name;
        private String iconUrl;
    }
}
