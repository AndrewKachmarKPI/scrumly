package com.scrumly.dto.backlog;

import com.scrumly.domain.backlog.IssueStatusEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class BacklogDto {
    private Long id;
    private String backlogId;
    @NotNull
    @NotEmpty
    @NotBlank
    @Size(max = 200)
    private String name;
    private String teamId;
    @NotNull
    @NotEmpty
    @NotBlank
    @Size(min = 2, max = 3)
    private String issueIdentifier;
    private LocalDateTime createdDateTime;
    private List<IssueStatusDto> issueStatuses;
    private List<IssueTypeDto> issueTypes;
    private List<IssueDto> issues;
}
