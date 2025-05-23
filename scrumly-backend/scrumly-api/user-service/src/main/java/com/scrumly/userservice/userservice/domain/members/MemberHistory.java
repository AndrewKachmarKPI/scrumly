package com.scrumly.userservice.userservice.domain.members;

import com.scrumly.userservice.userservice.domain.HistoryEntity;
import com.scrumly.userservice.userservice.enums.members.MemberChangeAction;
import com.scrumly.userservice.userservice.enums.members.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class MemberHistory extends HistoryEntity {
    @Column
    private MemberStatus previousStatus;
    @Column
    private MemberStatus newStatus;
    @Column(nullable = false, updatable = false)
    private MemberChangeAction changeAction;

    @Builder
    public MemberHistory(Long id, LocalDateTime dateTime, String performedBy, MemberStatus previousStatus, MemberStatus newStatus, MemberChangeAction changeAction) {
        super(id, dateTime, performedBy);
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changeAction = changeAction;
    }
}
