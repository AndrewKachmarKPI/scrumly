package com.scrumly.userservice.userservice.domain.team;

import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import jakarta.persistence.*;
import lombok.*;

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
public class TeamEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private String organizationId;
    @Column(nullable = false, unique = true, updatable = false)
    private String teamId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;
    @ManyToOne
    private UserProfileEntity createdBy;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMemberEntity> members;
}
