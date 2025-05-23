package com.scrumly.userservice.userservice.dto.keycloak;

import com.scrumly.userservice.userservice.dto.requests.UserRegisterRQ;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
public class KeycloakRegisterUserDto {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean enabled;
    private List<KeycloakCredentialDto> credentials;
    private List<String> groups;

    public KeycloakRegisterUserDto(UserRegisterRQ userRegisterRQ) {
        this.username = userRegisterRQ.getEmail();
        this.firstName = userRegisterRQ.getFirstName();
        this.lastName = userRegisterRQ.getLastName();
        this.email = userRegisterRQ.getEmail();
        this.enabled = true;
        this.credentials = List.of(KeycloakCredentialDto.builder()
                .value(userRegisterRQ.getPassword())
                .type(KeycloakCredentialDto.KeycloakCredentialType.PASSWORD.getType())
                .temporary(false)
                .build());
    }
}
