package com.scrumly.userservice.userservice.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;

@Data
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeycloakRoleDto {
    private String id;
    private String name;
    private String description;
    private Boolean scopeParamRequired;
    private Boolean composite;
    private Boolean clientRole;
    private String containerId;
}
