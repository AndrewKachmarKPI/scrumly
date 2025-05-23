package com.scrumly.integrationservice.messaging.dto;

import com.scrumly.enums.integration.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ExportIssueDto implements Serializable {
    private String connectingId;
    private ServiceType provider;
    private String activityName;
    private String activityId;
    private List<IssueChangesDto> issues;


    @Data
    @ToString
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IssueChangesDto implements Serializable {
        private String issueId;
        private String estimation;
        private String status;
        private String statusId;
        private String backlogId;
        private String assigneeId;
        private String assigneeEmail;
    }
}
