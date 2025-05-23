package com.scrumly.userservice.userservice.domain.organization;

import com.scrumly.userservice.userservice.domain.invites.InviteEntity;
import com.scrumly.userservice.userservice.domain.members.MemberEntity;
import com.scrumly.userservice.userservice.domain.members.MemberHistory;
import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import com.scrumly.userservice.userservice.enums.members.MemberStatus;
import com.scrumly.userservice.userservice.enums.organization.OrganizationMemberRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class OrganizationMemberEntity extends MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String organizationId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationMemberRole role;
    @ManyToOne
    private InviteEntity invite;

    @Builder
    public OrganizationMemberEntity(Long id, LocalDateTime joinDateTime, MemberStatus status, UserProfileEntity profile, InviteEntity invite, List<MemberHistory> changeHistory, Long id1, String organizationId, OrganizationMemberRole role) {
        super(id, joinDateTime, status, profile, changeHistory);
        this.id = id1;
        this.organizationId = organizationId;
        this.role = role;
        this.invite = invite;
    }
}
