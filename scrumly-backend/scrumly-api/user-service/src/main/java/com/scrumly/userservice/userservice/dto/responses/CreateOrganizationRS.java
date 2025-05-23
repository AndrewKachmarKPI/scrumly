package com.scrumly.userservice.userservice.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class CreateOrganizationRS {
    private String organizationId;
    private String teamId;
}
