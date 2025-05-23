package com.scrumly.userservice.userservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserGroups {
    ADMINS,
    SUPER_ADMINS,
    ORGANIZATION_ADMINS,
    TEAM_ADMINS,

    INTERNAL_TEAM,
    TEAM_MEMBERS,
    TEAM_LEADS,
    SCRUM_MASTERS,

    EXTERNAL_TEAM,
    STAKEHOLDERS
}
