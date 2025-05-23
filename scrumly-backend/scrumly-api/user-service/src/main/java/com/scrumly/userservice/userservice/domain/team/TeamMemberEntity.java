package com.scrumly.userservice.userservice.domain.team;

import com.scrumly.userservice.userservice.domain.invites.InviteEntity;
import com.scrumly.userservice.userservice.domain.members.MemberEntity;
import com.scrumly.userservice.userservice.domain.members.MemberHistory;
import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import com.scrumly.userservice.userservice.enums.TeamMemberRole;
import com.scrumly.userservice.userservice.enums.members.MemberStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class TeamMemberEntity extends MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamMemberRole role;
    @Column
    private String badge;

    @Builder
    public TeamMemberEntity(Long id, LocalDateTime joinDateTime, MemberStatus status, UserProfileEntity profile,
                            List<MemberHistory> changeHistory, Long id1, TeamMemberRole role,
                            String badge) {
        super(id, joinDateTime, status, profile, changeHistory);
        this.id = id1;
        this.role = role;
        this.badge = badge;
    }
}
