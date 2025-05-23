package com.scrumly.userservice.userservice.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserDto {
    @JsonProperty("id")
    private String id;
    @JsonProperty("createdTimestamp")
    private Long created;
    @JsonProperty("username")
    private String username;
    @JsonProperty("enabled")
    private Boolean enabled;
    @JsonProperty("emailVerified")
    private Boolean isEmailVerified;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("email")
    private String email;
}
