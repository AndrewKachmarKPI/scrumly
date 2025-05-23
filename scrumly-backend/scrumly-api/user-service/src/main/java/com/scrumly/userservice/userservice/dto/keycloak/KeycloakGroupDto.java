package com.scrumly.userservice.userservice.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeycloakGroupDto {
    private String id;
    private String name;
    private String path;
    private String parentId;
    private Long subGroupCount;
    private List<KeycloakGroupDto> subGroups;
    private List<String> realmRoles;

    public List<KeycloakGroupDto> getSubGroups() {
        return subGroups != null ? subGroups : new ArrayList<>();
    }
}
