package com.scrumly.userservice.userservice.services.impls;

import com.scrumly.userservice.userservice.dto.keycloak.KeycloakGroupDto;
import com.scrumly.userservice.userservice.dto.keycloak.KeycloakRoleDto;
import com.scrumly.userservice.userservice.enums.UserGroups;
import com.scrumly.userservice.userservice.enums.UserRole;
import com.scrumly.userservice.userservice.services.KeycloakInitService;
import com.scrumly.userservice.userservice.services.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeycloakInitServiceImpl implements KeycloakInitService {
    private final KeycloakService keycloakService;


    @Override
    public void init() {
        initUserRoles();
        initGroups();
        initSubgroups();
        initGroupRoleMapping();
    }

    private void initGroupRoleMapping() {
        List<KeycloakGroupDto> allGroups = getAllGroups();
        List<KeycloakRoleDto> allRoles = keycloakService.findAllRoles();
        mapGroupWithRole(allGroups, allRoles, UserGroups.SUPER_ADMINS, UserRole.SUPER_ADMIN);
        mapGroupWithRole(allGroups, allRoles, UserGroups.ORGANIZATION_ADMINS, UserRole.ORGANIZATION_ADMIN);
        mapGroupWithRole(allGroups, allRoles, UserGroups.TEAM_ADMINS, UserRole.TEAM_ADMIN);
        mapGroupWithRole(allGroups, allRoles, UserGroups.TEAM_MEMBERS, UserRole.TEAM_MEMBER);
        mapGroupWithRole(allGroups, allRoles, UserGroups.TEAM_LEADS, UserRole.TEAM_LEAD);
        mapGroupWithRole(allGroups, allRoles, UserGroups.SCRUM_MASTERS, UserRole.SCRUM_MASTER);
        mapGroupWithRole(allGroups, allRoles, UserGroups.STAKEHOLDERS, UserRole.STAKEHOLDER);
    }

    private void mapGroupWithRole(List<KeycloakGroupDto> allGroups,
                                  List<KeycloakRoleDto> allRoles,
                                  UserGroups group, UserRole role) {
        KeycloakGroupDto groupDto = findGroup(allGroups, group);
        KeycloakRoleDto roleDto = findRole(allRoles, role);
        keycloakService.mapRoleWithGroup(groupDto.getId(), roleDto);
    }

    private List<KeycloakGroupDto> getAllGroups() {
        return keycloakService.findAllGroups()
                .stream().flatMap(groupDto -> {
                    List<KeycloakGroupDto> subGroups = new ArrayList<>();
                    subGroups.add(groupDto);
                    subGroups.addAll(groupDto.getSubGroups());
                    return subGroups.stream();
                })
                .collect(Collectors.toList());
    }

    private void initSubgroups() {
        List<KeycloakGroupDto> groups = keycloakService.findAllGroups();

        KeycloakGroupDto adminsGroup = findGroup(groups, UserGroups.ADMINS);
        if (adminsGroup != null) {
            List<String> subgroupNames = adminsGroup.getSubGroups().stream()
                    .map(KeycloakGroupDto::getName)
                    .toList();
            if (!subgroupNames.contains(UserGroups.SUPER_ADMINS.name())) {
                createChildGroup(adminsGroup.getId(), UserGroups.SUPER_ADMINS);
            }
            if (!subgroupNames.contains(UserGroups.ORGANIZATION_ADMINS.name())) {
                createChildGroup(adminsGroup.getId(), UserGroups.ORGANIZATION_ADMINS);
            }
            if (!subgroupNames.contains(UserGroups.TEAM_ADMINS.name())) {
                createChildGroup(adminsGroup.getId(), UserGroups.TEAM_ADMINS);
            }
        }

        KeycloakGroupDto teamGroup = findGroup(groups, UserGroups.INTERNAL_TEAM);
        if (teamGroup != null) {
            List<String> subgroupNames = teamGroup.getSubGroups().stream()
                    .map(KeycloakGroupDto::getName)
                    .toList();
            if (!subgroupNames.contains(UserGroups.TEAM_MEMBERS.name())) {
                createChildGroup(teamGroup.getId(), UserGroups.TEAM_MEMBERS);
            }
            if (!subgroupNames.contains(UserGroups.TEAM_LEADS.name())) {
                createChildGroup(teamGroup.getId(), UserGroups.TEAM_LEADS);
            }
            if (!subgroupNames.contains(UserGroups.SCRUM_MASTERS.name())) {
                createChildGroup(teamGroup.getId(), UserGroups.SCRUM_MASTERS);
            }
        }

        KeycloakGroupDto externalTeamGroup = findGroup(groups, UserGroups.EXTERNAL_TEAM);
        if (externalTeamGroup != null) {
            List<String> subgroupNames = externalTeamGroup.getSubGroups().stream()
                    .map(KeycloakGroupDto::getName)
                    .toList();
            if (!subgroupNames.contains(UserGroups.STAKEHOLDERS.name())) {
                createChildGroup(externalTeamGroup.getId(), UserGroups.STAKEHOLDERS);
            }
        }
    }

    private static KeycloakGroupDto findGroup(List<KeycloakGroupDto> groups, UserGroups admins) {
        return groups.stream()
                .filter(groupDto -> groupDto.getName().equals(admins.toString()))
                .findFirst()
                .orElse(null);
    }

    private static KeycloakRoleDto findRole(List<KeycloakRoleDto> groups, UserRole role) {
        return groups.stream()
                .filter(roleDto -> roleDto.getName().equals(role.toString()))
                .findFirst()
                .orElse(null);
    }

    private void initGroups() {
        List<String> groupNames = keycloakService.findAllGroups().stream()
                .map(KeycloakGroupDto::getName)
                .toList();
        createGroup(groupNames, UserGroups.ADMINS);
        createGroup(groupNames, UserGroups.INTERNAL_TEAM);
        createGroup(groupNames, UserGroups.EXTERNAL_TEAM);
    }

    private void initUserRoles() {
        List<String> roles = keycloakService.findAllRoles().stream()
                .map(KeycloakRoleDto::getName)
                .toList();
        createRole(roles, UserRole.SUPER_ADMIN);
        createRole(roles, UserRole.ORGANIZATION_ADMIN);
        createRole(roles, UserRole.TEAM_ADMIN);
        createRole(roles, UserRole.TEAM_MEMBER);
        createRole(roles, UserRole.TEAM_LEAD);
        createRole(roles, UserRole.SCRUM_MASTER);
        createRole(roles, UserRole.STAKEHOLDER);
    }


    private void createChildGroup(String parentId, UserGroups group) {
        keycloakService.createChildGroup(parentId,
                KeycloakGroupDto.builder()
                        .name(group.name())
                        .build());
    }

    private void createGroup(List<String> groupNames, UserGroups admins) {
        if (!groupNames.contains(admins.name())) {
            keycloakService.createGroup(KeycloakGroupDto.builder()
                    .name(admins.name())
                    .build());
        }
    }

    private void createRole(List<String> roles, UserRole superAdmin) {
        if (!roles.contains(superAdmin.name())) {
            keycloakService.createRole(KeycloakRoleDto.builder()
                    .name(superAdmin.name())
                    .description("${" + superAdmin.name() + "}")
                    .build());
        }
    }
}
