package com.scrumly.domain.backlog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Entity
public class BacklogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String backlogId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String teamId;
    @Column(nullable = false)
    private String issueIdentifier;
    @Column(nullable = false)
    private LocalDateTime createdDateTime;
    @OneToMany(fetch = FetchType.LAZY)
    private List<IssueStatusEntity> issueStatuses;
    @OneToMany(fetch = FetchType.LAZY)
    private List<IssueTypeEntity> issueTypes;
    @OneToMany(fetch = FetchType.LAZY)
    private List<IssueEntity> issues;
}
