package com.scrumly.userservice.userservice.dto.service.organization;

import com.scrumly.userservice.userservice.dto.service.team.TeamDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationTeamGroupDto {
    private OrganizationDto organization;
    private List<TeamDto> teams;
}
