package com.scrumly.userservice.userservice.dto.service.organization;

import com.scrumly.userservice.userservice.dto.service.team.TeamDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import com.scrumly.userservice.userservice.enums.organization.OrganizationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationDto {
    private Long id;
    private String organizationId;
    private String name;
    private String logo;
    private String about;
    private OrganizationStatus status;
    private Boolean isActive;
    private LocalDateTime created;
    private UserProfileDto createdBy;
    private List<TeamDto> teams;
    private List<OrganizationMemberDto> members;
    private List<OrganizationHistoryDto> changeHistory;
}
