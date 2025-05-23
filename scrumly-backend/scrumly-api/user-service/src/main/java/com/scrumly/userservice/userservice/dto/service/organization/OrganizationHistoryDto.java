package com.scrumly.userservice.userservice.dto.service.organization;

import com.scrumly.userservice.userservice.enums.organization.OrganizationChangeAction;
import com.scrumly.userservice.userservice.enums.organization.OrganizationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationHistoryDto {
    private Long id;
    private LocalDateTime dateTime;
    private String performedBy;
    private OrganizationStatus previousStatus;
    private OrganizationStatus newStatus;
    private OrganizationChangeAction changeAction;
}
