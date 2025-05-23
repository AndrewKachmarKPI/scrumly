package com.scrumly.userservice.userservice.domain.organization;

import com.scrumly.userservice.userservice.domain.team.TeamEntity;
import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import com.scrumly.userservice.userservice.domain.ImageEntity;
import com.scrumly.userservice.userservice.enums.organization.OrganizationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class OrganizationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, updatable = false)
    private String organizationId;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(length = 1000)
    private String about;
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private ImageEntity logo;
    @Enumerated(EnumType.STRING)
    private OrganizationStatus status;
    @Column(nullable = false)
    private Boolean isActive;
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;
    @ManyToOne
    private UserProfileEntity createdBy;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<TeamEntity> teams = new ArrayList<>();
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrganizationMemberEntity> members = new ArrayList<>();
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    private List<OrganizationHistory> changeHistory = new ArrayList<>();
}
