package com.scrumly.dto.backlog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class IssueExportRefDto {
    private Long id;
    private String serviceName;
    private String projectName;
    private String serviceIssueKey;
    private String issueUrl;
    private String issueTypeUrl;
    private String issueTypeName;
    private LocalDateTime exportedDate;
}
