package com.scrumly.userservice.userservice.domain.members;

import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import com.scrumly.userservice.userservice.domain.invites.InviteEntity;
import com.scrumly.userservice.userservice.enums.members.MemberStatus;
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
@MappedSuperclass
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime joinDateTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private UserProfileEntity profile;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MemberHistory> changeHistory;
}
