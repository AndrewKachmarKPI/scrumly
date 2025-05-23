package com.scrumly.domain.backlog;

import com.scrumly.enums.integration.ServiceType;
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
@Entity
public class IssueExportRefEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String serviceName;
    @Column
    private String projectName;
    @Column(nullable = false)
    private String serviceIssueKey;
    @Column(nullable = false)
    private String issueUrl;
    @Column(nullable = false)
    private String issueTypeUrl;
    @Column(nullable = false)
    private String issueTypeName;
    @Column(nullable = false)
    private LocalDateTime exportedDate;
}
