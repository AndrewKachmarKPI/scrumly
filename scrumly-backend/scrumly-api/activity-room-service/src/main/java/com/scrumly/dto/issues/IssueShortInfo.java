package com.scrumly.dto.issues;

import com.scrumly.enums.integration.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class IssueShortInfo {
    private String issueId;
    private String issueKey;
    private String title;
    private String imgPath;
    private String issueUrl;
    private String projectName;
    private String projectId;
    private ServiceType provider;
    private String estimate;
}
