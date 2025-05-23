package com.scrumly.userservice.userservice.services.impls;

import com.scrumly.exceptions.enums.ServiceErrorCode;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.specification.*;
import com.scrumly.userservice.userservice.domain.members.MemberHistory;
import com.scrumly.userservice.userservice.domain.organization.OrganizationEntity;
import com.scrumly.userservice.userservice.domain.organization.OrganizationMemberEntity;
import com.scrumly.userservice.userservice.domain.team.TeamEntity;
import com.scrumly.userservice.userservice.domain.team.TeamMemberEntity;
import com.scrumly.userservice.userservice.domain.user.OrganizationConnectionEntity;
import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import com.scrumly.userservice.userservice.dto.requests.CreateTeamRQ;
import com.scrumly.userservice.userservice.dto.requests.UpdateTeamMemberRQ;
import com.scrumly.userservice.userservice.dto.requests.UpdateTeamRQ;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationDto;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationTeamGroupDto;
import com.scrumly.userservice.userservice.dto.service.team.TeamDto;
import com.scrumly.userservice.userservice.dto.service.team.TeamMemberDto;
import com.scrumly.userservice.userservice.dto.service.team.TeamMetadataDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import com.scrumly.userservice.userservice.enums.TeamMemberRole;
import com.scrumly.userservice.userservice.enums.members.MemberChangeAction;
import com.scrumly.userservice.userservice.enums.members.MemberStatus;
import com.scrumly.userservice.userservice.enums.organization.OrganizationMemberRole;
import com.scrumly.userservice.userservice.mappers.BusinessMapper;
import com.scrumly.userservice.userservice.repository.OrganizationRepository;
import com.scrumly.userservice.userservice.repository.TeamRepository;
import com.scrumly.userservice.userservice.services.OrganizationService;
import com.scrumly.userservice.userservice.services.TeamService;
import com.scrumly.userservice.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.scrumly.exceptions.enums.ServiceErrorCode.TEAM_MEMBER_NOTFOUND;
import static com.scrumly.userservice.userservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final UserService userService;
    private final BusinessMapper businessMapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationService organizationService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TeamDto createTeam(CreateTeamRQ createTeamRQ) {
        OrganizationEntity organization = organizationService.findOrganizationOrThrow(createTeamRQ.getOrganizationId());
        OrganizationMemberEntity member = organizationService
                .getOrganizationMember(createTeamRQ.getOrganizationId(), getUsername());
        if (member.getRole().equals(OrganizationMemberRole.MEMBER)) {
            throw new ServiceErrorException(ServiceErrorCode.ORGANIZATION_MEMBER_INSUFFICIENT_PERMISSION, "You are not allowed to create team");
        }

        TeamEntity team = teamRepository
                .findByNameAndOrganizationId(createTeamRQ.getTeamName(), createTeamRQ.getOrganizationId());
        if (team != null) {
            throw new EntityNotFoundException(ServiceErrorCode.DUPLICATE_TEAM);
        }

        if (createTeamRQ.getInviteMembers() != null && !createTeamRQ.getInviteMembers().isEmpty()) {
            if (!organizationService.checkIfExistsInOrganization(organization.getOrganizationId(), createTeamRQ.getInviteMembers())) {
                throw new ServiceErrorException(ServiceErrorCode.ORGANIZATION_MEMBER_INSUFFICIENT_PERMISSION, "Some of the members are not part of organization");
            }
        }


        LocalDateTime createDate = LocalDateTime.now();
        UserProfileEntity creator = userService.getUserProfileOrThrow(getUsername());

        List<TeamMemberEntity> teamMembers = new ArrayList<>();
        if (createTeamRQ.getInviteMembers() != null && !createTeamRQ.getInviteMembers().isEmpty()) {
            teamMembers = createNewTeamMembers(createTeamRQ.getInviteMembers());
        }
        teamMembers.add(TeamMemberEntity.builder()
                                .role(TeamMemberRole.TEAM_ADMIN)
                                .joinDateTime(createDate)
                                .status(MemberStatus.ACTIVE)
                                .profile(creator)
                                .changeHistory(List.of(
                                        MemberHistory.builder()
                                                .previousStatus(null)
                                                .dateTime(createDate)
                                                .newStatus(MemberStatus.ACTIVE)
                                                .performedBy(getUsername())
                                                .changeAction(MemberChangeAction.MEMBER_CREATED)
                                                .build()
                                ))
                                .build());

        team = TeamEntity.builder()
                .organizationId(createTeamRQ.getOrganizationId())
                .teamId(UUID.randomUUID().toString())
                .name(createTeamRQ.getTeamName())
                .created(createDate)
                .createdBy(creator)
                .members(teamMembers)
                .build();

        team = teamRepository.save(team);

        organization.getTeams().add(team);
        organizationRepository.save(organization);
        return businessMapper.teamToDto(team);
    }

    private List<TeamMemberEntity> createNewTeamMembers(List<String> inviteMembers) {
        return inviteMembers.stream()
                .map(inviteMember -> {
                    UserProfileEntity memberProfile = userService.getUserProfileOrThrow(inviteMember);
                    return TeamMemberEntity.builder()
                            .role(TeamMemberRole.MEMBER)
                            .joinDateTime(LocalDateTime.now())
                            .status(MemberStatus.ACTIVE)
                            .profile(memberProfile)
                            .changeHistory(List.of(
                                    MemberHistory.builder()
                                            .previousStatus(null)
                                            .dateTime(LocalDateTime.now())
                                            .newStatus(MemberStatus.ACTIVE)
                                            .performedBy(getUsername())
                                            .changeAction(MemberChangeAction.MEMBER_CREATED)
                                            .build()
                            ))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TeamDto updateTeam(String teamId, UpdateTeamRQ updateTeamRQ) {
        TeamEntity team = teamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.TEAM_NOTFOUND));
        TeamMemberEntity member = getTeamMember(team, getUsername());
        if (member.getRole().equals(TeamMemberRole.MEMBER)) {
            throw new ServiceErrorException(ServiceErrorCode.ORGANIZATION_MEMBER_INSUFFICIENT_PERMISSION, "You are not allowed to invite new members");
        }

        TeamMemberEntity performerMember = getTeamMember(team, getUsername());


        if (updateTeamRQ.getTeamName() != null) {
            if (teamRepository.existsByTeamIdAndOrganizationIdAndName(teamId, team.getOrganizationId(), updateTeamRQ.getTeamName())) {
                throw new DuplicateEntityException(ServiceErrorCode.DUPLICATE_TEAM);
            }
            team.setName(updateTeamRQ.getTeamName());
        }

        if (updateTeamRQ.getUpdateMembers() != null && !updateTeamRQ.getUpdateMembers().isEmpty()) {
            List<String> memberUserIds = updateTeamRQ.getUpdateMembers().stream()
                    .map(UpdateTeamMemberRQ::getUserId)
                    .toList();
            if (!checkIfExistsInTeam(team.getTeamId(), memberUserIds)) {
                throw new ServiceErrorException(ServiceErrorCode.TEAM_MEMBER_INSUFFICIENT_PERMISSION, "Some of users are not part of a team");
            }

            for (UpdateTeamMemberRQ updateMember : updateTeamRQ.getUpdateMembers()) {
                TeamMemberEntity memberEntity = getTeamMember(team, updateMember.getUserId());
                if (updateMember.getBadge() != null) {
                    memberEntity.setBadge(updateMember.getBadge());
                }
                if (updateMember.getMemberRole() != null) {
                    if (performerMember.getRole().equals(memberEntity.getRole()) ||
                            memberEntity.getRole().equals(TeamMemberRole.TEAM_ADMIN)) {
                        throw new ServiceErrorException(ServiceErrorCode.TEAM_MEMBER_INSUFFICIENT_PERMISSION, "You cannot change such team member");
                    }
                    memberEntity.setRole(updateMember.getMemberRole());
                }
            }
        }

        team = teamRepository.save(team);
        return businessMapper.teamToDto(team);
    }

    @Override
    @Transactional
    public TeamDto inviteTeamMembers(String teamId, List<String> inviteMembers) {
        TeamEntity team = teamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.TEAM_NOTFOUND));
        TeamMemberEntity member = getTeamMember(team, getUsername());
        if (member.getRole().equals(TeamMemberRole.MEMBER)) {
            throw new ServiceErrorException(ServiceErrorCode.ORGANIZATION_MEMBER_INSUFFICIENT_PERMISSION, "You are not allowed to invite new members");
        }
        if (!organizationService.checkIfExistsInOrganization(team.getOrganizationId(), inviteMembers)) {
            throw new ServiceErrorException(ServiceErrorCode.ORGANIZATION_MEMBER_INSUFFICIENT_PERMISSION, "Some of the members are not part of organization");
        }
        if (checkIfExistsInTeam(teamId, inviteMembers)) {
            throw new ServiceErrorException(ServiceErrorCode.ORGANIZATION_MEMBER_INSUFFICIENT_PERMISSION, "Some of the members are already part of this team");
        }

        List<TeamMemberEntity> teamMembers = createNewTeamMembers(inviteMembers);
        team.getMembers().addAll(teamMembers);
        team = teamRepository.save(team);
        return businessMapper.teamToDto(team);
    }

    @Override
    public TeamDto removeTeamMembers(String teamId, List<String> userIds) {
        userIds.forEach(userId -> removeTeamMember(teamId, userId));
        TeamEntity team = teamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.TEAM_NOTFOUND));
        return businessMapper.teamToDto(team);
    }

    public void removeTeamMember(String teamId, String userId) {
        TeamEntity team = teamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.TEAM_NOTFOUND));
        TeamMemberEntity member = getTeamMember(team, getUsername());
        TeamMemberEntity memberToBeDeleted = getTeamMember(team, userId);

        if (member.getRole().equals(TeamMemberRole.MEMBER)) {
            throw new ServiceErrorException(ServiceErrorCode.ORGANIZATION_MEMBER_INSUFFICIENT_PERMISSION, "You are not allowed to remove members");
        }
        if (memberToBeDeleted.getRole().equals(TeamMemberRole.TEAM_ADMIN)) {
            throw new ServiceErrorException(ServiceErrorCode.TEAM_MEMBER_INSUFFICIENT_PERMISSION, "You are not allowed to remove team admin");
        }

        team.getMembers().remove(memberToBeDeleted);
        team = teamRepository.save(team);
        businessMapper.teamToDto(team);
    }

    @Override
    public PageDto<TeamDto> findTeams(SearchQuery searchQuery) {
        Specification<TeamEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        Page<TeamEntity> page = teamRepository.findAll(specification, pageable);
        return GeneralSpecification.getPageResponse(page, businessMapper::teamToDto);
    }

    @Override
    public PageDto<TeamDto> findMyTeams(SearchQuery searchQuery) {
        searchQuery.appendSearchFilter(new SearchFilter("members.profile.userId", SearchOperators.IN.name(), CompareOption.AND, getUsername()));
        Specification<TeamEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        Page<TeamEntity> page = teamRepository.findAll(specification, pageable);
        return GeneralSpecification.getPageResponse(page, businessMapper::teamToDto);
    }

    @Override
    public PageDto<TeamDto> findTeamsByOrganizationId(SearchQuery searchQuery, String orgId) {
        searchQuery.appendSearchFilter(new SearchFilter("organizationId", SearchOperators.EQUALS, CompareOption.AND, orgId));
        return findTeams(searchQuery);
    }

    public List<TeamDto> findTeamsByOrganizationId(String orgId) {
        return teamRepository.findAllByOrganizationId(orgId)
                .stream().map(businessMapper::teamToDto).toList();
    }

    @Override
    public TeamDto findTeamById(String teamId) {
        TeamEntity team = teamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.TEAM_NOTFOUND));
        return businessMapper.teamToDto(team);
    }

    @Override
    @Transactional
    public void deleteTeamById(String teamId) {
        TeamEntity team = teamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.TEAM_NOTFOUND));
        TeamMemberEntity member = getTeamMember(team, getUsername());
        if (!member.getRole().equals(TeamMemberRole.TEAM_ADMIN)) {
            throw new ServiceErrorException(ServiceErrorCode.TEAM_MEMBER_INSUFFICIENT_PERMISSION, "You cannot delete this team");
        }

        OrganizationEntity organization = organizationService.findOrganizationOrThrow(team.getOrganizationId());
        organization.getTeams().removeIf(orgTeam -> orgTeam.getTeamId().equals(teamId));

        organizationRepository.save(organization);
        teamRepository.delete(team);
    }

    @Override
    public boolean checkIfExistsInTeam(String teamId, List<String> userIds) {
        TeamEntity team = teamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.TEAM_NOTFOUND));
        return team.getMembers().stream()
                .anyMatch(member -> member.getProfile() != null && userIds.contains(member.getProfile().getUserId()));
    }

    @Override
    public List<OrganizationTeamGroupDto> findOrganizationTeamGroup(String userId) {
        List<OrganizationTeamGroupDto> groups = new ArrayList<>();
        UserProfileEntity userProfile = userService.getUserProfileOrThrow(userId);
        List<String> orgIds = userProfile.getConnectedOrganizations().stream()
                .map(OrganizationConnectionEntity::getOrganizationId)
                .toList();
        for (String orgId : orgIds) {
            groups.add(OrganizationTeamGroupDto.builder()
                               .organization(organizationService.findOrganization(orgId))
                               .teams(findTeamsByOrganizationId(orgId))
                               .build());
        }
        return groups;
    }

    @Override
    public TeamMetadataDto findTeamMetadata(String teamId) {
        TeamEntity team = teamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.TEAM_NOTFOUND));
        OrganizationDto organization = organizationService.findOrganization(team.getOrganizationId());
        return TeamMetadataDto.builder()
                .organizationId(team.getOrganizationId())
                .teamId(team.getTeamId())
                .teamName(team.getName())
                .organizationName(organization.getName())
                .build();
    }

    @Override
    public List<UserProfileDto> findTeamUsers(String teamId) {
        TeamEntity team = teamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.TEAM_NOTFOUND));
        return businessMapper.teamMembersToDto(team.getMembers())
                .stream().map(TeamMemberDto::getProfile)
                .collect(Collectors.toList());
    }

    private TeamMemberEntity getTeamMember(TeamEntity team, String userId) {
        return team.getMembers().stream()
                .filter(teamMemberEntity -> teamMemberEntity.getProfile().getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(TEAM_MEMBER_NOTFOUND));
    }

    private TeamMemberEntity getTeamMember(String teamId, String userId) {
        TeamEntity team = teamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.TEAM_NOTFOUND));
        return getTeamMember(team, userId);
    }
}
