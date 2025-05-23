package com.scrumly.userservice.userservice.services;

import com.scrumly.userservice.userservice.dto.keycloak.KeycloakGroupDto;
import com.scrumly.userservice.userservice.dto.keycloak.KeycloakRegisterUserDto;
import com.scrumly.userservice.userservice.dto.keycloak.KeycloakRoleDto;
import com.scrumly.userservice.userservice.dto.keycloak.KeycloakUserDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import org.springframework.http.HttpHeaders;

import java.util.List;

public interface KeycloakService {
    KeycloakUserDto createUser(KeycloakRegisterUserDto registerUserDto);
    void updateUser(String username, UserProfileDto userProfileDto);
    void deleteUser(String userId);

    KeycloakUserDto findUserByName(String username);

    KeycloakUserDto findUserByName(String username, HttpHeaders headers);

    void addGroupMember(String username, String groupName);

    void createRole(KeycloakRoleDto roleDto);

    List<KeycloakRoleDto> findAllRoles();

    void createGroup(KeycloakGroupDto groupDto);

    void createChildGroup(String parentId, KeycloakGroupDto groupDto);
    List<KeycloakGroupDto> findAllGroups();

    void mapRoleWithGroup(String groupId, KeycloakRoleDto roleDto);
}
