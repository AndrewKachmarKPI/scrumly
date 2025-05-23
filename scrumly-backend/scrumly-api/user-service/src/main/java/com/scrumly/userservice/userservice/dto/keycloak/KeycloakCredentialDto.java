package com.scrumly.userservice.userservice.dto.keycloak;

import lombok.*;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakCredentialDto {
    private String value;
    private String type;
    private boolean temporary;

    @Getter
    @AllArgsConstructor
    public enum KeycloakCredentialType {
        PASSWORD("password");
        private final String type;
    }
}
