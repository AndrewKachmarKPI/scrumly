package com.scrumly.userservice.userservice.services.impls;

import com.scrumly.userservice.userservice.dto.keycloak.*;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import com.scrumly.userservice.userservice.exceptions.KeycloakException;
import com.scrumly.userservice.userservice.services.KeycloakService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeycloakServiceImpl implements KeycloakService {
    private static final String ADMIN_PATH = "/admin/realms/";

    @Value("${keycloak.url}")
    private String keycloakUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.token-path}")
    private String masterTokenUrl;
    @Value("${keycloak.master-username}")
    private String masterUsername;
    @Value("${keycloak.master-password}")
    private String masterPassword;

    @Override
    public KeycloakUserDto createUser(KeycloakRegisterUserDto registerUserDto) {
        WebClient.create(keycloakUrl).post()
                .uri(ADMIN_PATH + realm + "/users")
                .bodyValue(registerUserDto)
                .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
                .retrieve()
                .bodyToFlux(Void.class)
                .onErrorMap(e -> new KeycloakException(e.getMessage()))
                .blockLast();
        return findUserByName(registerUserDto.getUsername());
    }

    @Override
    public void deleteUser(String userId) {
        WebClient.create(keycloakUrl).delete()
                .uri(ADMIN_PATH + realm + "/users/" + userId)
                .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
                .retrieve()
                .bodyToFlux(Void.class)
                .onErrorMap(e -> new KeycloakException(e.getMessage()))
                .blockLast();
    }

    @Override
    public KeycloakUserDto findUserByName(String username) {
        return findUserByName(username, getHeaders());
    }

    @Override
    public KeycloakUserDto findUserByName(String username, HttpHeaders headers) {
        return WebClient.create(keycloakUrl).get()
                .uri(uriBuilder ->
                        uriBuilder.path(ADMIN_PATH + realm + "/users")
                                .queryParam("username", username)
                                .queryParam("exact", true)
                                .queryParam("briefRepresentation", true)
                                .build()
                )
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .bodyToFlux(KeycloakUserDto.class)
                .onErrorMap(e -> new KeycloakException(e.getMessage()))
                .blockLast();
    }

    @Override
    public void addGroupMember(String username, String groupName) {
        HttpHeaders headers = getHeaders();
        KeycloakUserDto keycloakUserDto = findUserByName(username, headers);
        KeycloakGroupDto keycloakGroupDto = findGroupByName(groupName, headers);
        WebClient.create(keycloakUrl).put()
                .uri(ADMIN_PATH + realm + "/users/" + keycloakUserDto.getId() + "/groups/" + keycloakGroupDto.getId())
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .bodyToFlux(Void.class)
                .onErrorMap(e -> new KeycloakException(e.getMessage()))
                .blockLast();
    }

    @Override
    public void createRole(KeycloakRoleDto roleDto) {
        WebClient.create(keycloakUrl).post()
                .uri(ADMIN_PATH + realm + "/roles")
                .bodyValue(roleDto)
                .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
                .retrieve()
                .bodyToFlux(KeycloakRoleDto.class)
                .onErrorMap(e -> new KeycloakException(e.getMessage()))
                .blockLast();
    }

    @Override
    public List<KeycloakRoleDto> findAllRoles() {
        return WebClient.create(keycloakUrl).get()
                .uri(ADMIN_PATH + realm + "/roles")
                .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<KeycloakRoleDto>>() {
                })
                .onErrorMap(e -> new KeycloakException(e.getMessage()))
                .block();
    }

    @Override
    public void createGroup(KeycloakGroupDto groupDto) {
        WebClient.create(keycloakUrl).post()
                .uri(ADMIN_PATH + realm + "/groups")
                .bodyValue(groupDto)
                .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
                .retrieve()
                .bodyToFlux(Void.class)
                .onErrorMap(e -> new KeycloakException(e.getMessage()))
                .blockLast();
    }

    @Override
    public void createChildGroup(String parentId, KeycloakGroupDto groupDto) {
        WebClient.create(keycloakUrl).post()
                .uri(ADMIN_PATH + realm + "/groups/" + parentId + "/children")
                .bodyValue(groupDto)
                .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
                .retrieve()
                .bodyToFlux(Void.class)
                .onErrorMap(e -> new KeycloakException(e.getMessage()))
                .blockLast();
    }

    @Override
    public List<KeycloakGroupDto> findAllGroups() {
        return WebClient.create(keycloakUrl).get()
                .uri(ADMIN_PATH + realm + "/groups")
                .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<KeycloakGroupDto>>() {
                })
                .onErrorMap(e -> new KeycloakException(e.getMessage()))
                .block();
    }

    @Override
    public void mapRoleWithGroup(String groupId, KeycloakRoleDto roleDto) {
        WebClient.create(keycloakUrl).post()
                .uri(ADMIN_PATH + realm + "/groups/" + groupId + "/role-mappings/realm")
                .bodyValue(List.of(roleDto))
                .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(e -> new KeycloakException(e.getMessage()))
                .block();
    }

    @Override
    public void updateUser(String username, UserProfileDto userProfileDto) {
        HttpHeaders headers = getHeaders();
        KeycloakUserDto keycloakUserDto = findUserByName(username, headers);
        keycloakUserDto = keycloakUserDto.toBuilder()
                .firstName(userProfileDto.getFirstName())
                .lastName(userProfileDto.getLastName())
                .email(userProfileDto.getEmail())
                .build();
        WebClient.create(keycloakUrl).put()
                .uri(ADMIN_PATH + realm + "/users/" + keycloakUserDto.getId())
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .bodyValue(keycloakUserDto)
                .retrieve()
                .bodyToFlux(Void.class)
                .onErrorMap(e -> new KeycloakException(e.getMessage()))
                .blockLast();
    }

    private KeycloakGroupDto findGroupByName(String groupName, HttpHeaders headers) {
        List<KeycloakGroupDto> keycloakGroupDtoList = WebClient.create(keycloakUrl).get()
                .uri(uriBuilder -> uriBuilder.path(ADMIN_PATH + realm + "/groups").build())
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<KeycloakGroupDto>>() {
                })
                .block();
        return keycloakGroupDtoList != null ? keycloakGroupDtoList.stream()
                .flatMap(groupDto -> {
                    List<KeycloakGroupDto> groups = new ArrayList<>();
                    groups.add(groupDto);
                    groups.addAll(groupDto.getSubGroups());
                    return groups.stream();
                })
                .filter(groupDto -> groupDto.getName().equals(groupName))
                .findFirst()
                .orElseThrow(() -> new KeycloakException("User group not found")) : null;
    }

    private KeycloakAccessTokenResponse getServiceAccessToken() {
        return WebClient.create(keycloakUrl).post()
                .uri(masterTokenUrl)
                .body(BodyInserters
                        .fromFormData("grant_type", "password")
                        .with("client_id", "admin-cli")
                        .with("username", masterUsername)
                        .with("password", masterPassword))
                .retrieve()
                .bodyToFlux(KeycloakAccessTokenResponse.class)
                .onErrorMap(e -> {
                    e.printStackTrace();
                    return new KeycloakException(e.getMessage());
                })
                .blockLast();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        KeycloakAccessTokenResponse tokenResponse = getServiceAccessToken();
        if (tokenResponse == null) {
            throw new KeycloakException("Cannot get service access token");
        }
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.AUTHORIZATION, tokenResponse.getAccessToken());
        return headers;
    }
}
