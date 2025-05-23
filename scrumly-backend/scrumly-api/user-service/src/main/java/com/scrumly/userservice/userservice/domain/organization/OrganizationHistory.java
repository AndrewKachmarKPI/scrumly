package com.scrumly.userservice.userservice.domain.organization;

import com.scrumly.userservice.userservice.domain.HistoryEntity;
import com.scrumly.userservice.userservice.enums.organization.OrganizationChangeAction;
import com.scrumly.userservice.userservice.enums.organization.OrganizationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class OrganizationHistory extends HistoryEntity {
    @Column
    private OrganizationStatus previousStatus;
    @Column
    private OrganizationStatus newStatus;
    @Column(nullable = false, updatable = false)
    private OrganizationChangeAction changeAction;

    @Builder
    public OrganizationHistory(Long id, LocalDateTime dateTime, String performedBy, OrganizationStatus previousStatus, OrganizationStatus newStatus, OrganizationChangeAction changeAction) {
        super(id, dateTime, performedBy);
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changeAction = changeAction;
    }
}
