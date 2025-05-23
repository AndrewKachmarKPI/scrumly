package com.scrumly.userservice.userservice.dto.service.user;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationConnectionDto {
    private Long id;
    private String organizationId;
    private Boolean isActive;
    private LocalDateTime dateConnected;
}
