package com.scrumly.userservice.userservice.domain.invites;

import com.scrumly.userservice.userservice.enums.invite.InviteStatus;
import com.scrumly.userservice.userservice.enums.invite.InviteType;
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
public class InviteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false, unique = true)
    private String inviteId;
    @Column(nullable = false)
    private String inviteUrl;
    @Column(nullable = false)
    private String connectingId;
    @Column(nullable = false, updatable = false)
    private InviteType inviteType;
    @Column(nullable = false)
    private InviteStatus currentStatus;
    @Column(nullable = false, updatable = false)
    private String createBy;
    @Column(nullable = false, updatable = false)
    private String createdFor;
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;
    @Column
    private LocalDateTime accepted;
    @Column(nullable = false, updatable = false)
    private LocalDateTime expiresAt;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InviteHistoryEntity> changeLog;
}
