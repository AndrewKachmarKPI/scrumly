package com.scrumly.dto.backlog;

import com.scrumly.dto.user.UserProfileDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class IssueDto {
    private Long id;
    private String backlogId;
    private String teamId;
    private String backlogName;
    private String issueKey;
    private String title;
    private String description;
    private LocalDateTime createdDateTime;
    private UserProfileDto createdBy;
    private IssueStatusDto status;
    private UserProfileDto assignee;
    private IssueTypeDto issueType;
    private IssueEstimationDto issueEstimation;
    private List<IssueExportRefDto> exportRefs;
}
