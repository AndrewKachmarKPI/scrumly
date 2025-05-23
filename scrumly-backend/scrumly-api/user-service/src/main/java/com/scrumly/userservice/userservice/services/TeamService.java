package com.scrumly.userservice.userservice.services;

import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import com.scrumly.userservice.userservice.dto.requests.CreateTeamRQ;
import com.scrumly.userservice.userservice.dto.requests.UpdateTeamRQ;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationTeamGroupDto;
import com.scrumly.userservice.userservice.dto.service.team.TeamDto;
import com.scrumly.userservice.userservice.dto.service.team.TeamMetadataDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;

import java.util.List;

public interface TeamService {
    TeamDto createTeam(CreateTeamRQ createTeamRQ);

    TeamDto updateTeam(String teamId, UpdateTeamRQ updateTeamRQ);

    PageDto<TeamDto> findTeams(SearchQuery searchQuery);

    PageDto<TeamDto> findMyTeams(SearchQuery searchQuery);

    PageDto<TeamDto> findTeamsByOrganizationId(SearchQuery searchQuery, String orgId);

    TeamDto findTeamById(String teamId);

    void deleteTeamById(String teamId);

    TeamDto inviteTeamMembers(String teamId, List<String> inviteMembers);

    TeamDto removeTeamMembers(String teamId, List<String> members);

    boolean checkIfExistsInTeam(String teamId, List<String> userIds);

    List<OrganizationTeamGroupDto> findOrganizationTeamGroup(String userId);

    TeamMetadataDto findTeamMetadata(String teamId);

    List<UserProfileDto> findTeamUsers(String teamId);
}
