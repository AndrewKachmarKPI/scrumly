package com.scrumly.userservice.userservice.dto.service.organization;

import com.scrumly.userservice.userservice.dto.service.user.UserInfoDto;
import com.scrumly.userservice.userservice.enums.organization.OrganizationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationInfoDto {
    private String organizationId;
    private String name;
    private String about;
    private String logo;
    private OrganizationStatus status;
    private Boolean isActive;
    private LocalDateTime created;
    private UserInfoDto createdBy;
    private Integer numberOfTeams;
    private Integer numberOfMembers;
    private Boolean isOrgAccessBlocked;
    private OrganizationMemberDto orgMember;
}
