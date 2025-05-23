package com.scrumly.userservice.userservice.domain.invites;

import com.scrumly.userservice.userservice.domain.HistoryEntity;
import com.scrumly.userservice.userservice.enums.invite.InviteChangeAction;
import com.scrumly.userservice.userservice.enums.invite.InviteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class InviteHistoryEntity extends HistoryEntity {
    @Column
    private InviteStatus previousStatus;
    @Column
    private InviteStatus newStatus;
    @Column(nullable = false, updatable = false)
    private InviteChangeAction changeAction;

    @Builder
    public InviteHistoryEntity(Long id, LocalDateTime dateTime, String performedBy, InviteStatus previousStatus, InviteStatus newStatus, InviteChangeAction changeAction) {
        super(id, dateTime, performedBy);
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changeAction = changeAction;
    }
}
