package com.scrumly.userservice.userservice.mappers;

import com.scrumly.userservice.userservice.domain.invites.InviteEntity;
import com.scrumly.userservice.userservice.domain.members.MemberHistory;
import com.scrumly.userservice.userservice.domain.organization.OrganizationEntity;
import com.scrumly.userservice.userservice.domain.organization.OrganizationHistory;
import com.scrumly.userservice.userservice.domain.organization.OrganizationMemberEntity;
import com.scrumly.userservice.userservice.domain.team.TeamEntity;
import com.scrumly.userservice.userservice.domain.team.TeamMemberEntity;
import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import com.scrumly.userservice.userservice.dto.service.MemberHistoryDto;
import com.scrumly.userservice.userservice.dto.service.invite.InviteDto;
import com.scrumly.userservice.userservice.dto.service.organization.*;
import com.scrumly.userservice.userservice.dto.service.team.TeamDto;
import com.scrumly.userservice.userservice.dto.service.team.TeamMemberDto;
import com.scrumly.userservice.userservice.dto.service.user.UserInfoDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import com.scrumly.userservice.userservice.enums.invite.InviteStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.scrumly.userservice.userservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class BusinessMapper {
    private final ModelMapper modelMapper;


    public OrganizationDto organizationToDto(OrganizationEntity entity) {
        return OrganizationDto.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .name(entity.getName())
                .logo(entity.getLogo() != null ? entity.getLogo().getImageId() : null)
                .status(entity.getStatus())
                .about(entity.getAbout())
                .created(entity.getCreated())
                .createdBy(modelMapper.map(entity.getCreatedBy(), UserProfileDto.class))
                .teams(teamsToDto(entity.getTeams()))
                .members(orgMembersToDto(entity.getMembers()))
                .changeHistory(orgHistoryToDto(entity.getChangeHistory()))
                .build();
    }

    public OrganizationInfoDto organizationToInfoDto(OrganizationEntity entity) {
        String username = getUsername();
        OrganizationMemberEntity member = entity.getMembers().stream()
                .filter(mem -> mem.getProfile().getUserId().equals(username))
                .findFirst().orElse(null);
        boolean isOrgAccessBlocked = false;
        if (member != null) {
            isOrgAccessBlocked = member.getProfile().getConnectedOrganizations()
                    .stream()
                    .anyMatch(orgCon -> orgCon.getOrganizationId().equals(entity.getOrganizationId()) && !orgCon.getIsActive());
        }
        return OrganizationInfoDto.builder()
                .organizationId(entity.getOrganizationId())
                .name(entity.getName())
                .about(entity.getAbout())
                .logo(entity.getLogo() != null ? entity.getLogo().getImageId() : null)
                .isActive(entity.getIsActive())
                .about(entity.getAbout())
                .status(entity.getStatus())
                .created(entity.getCreated())
                .createdBy(userProfileToInfoDto(entity.getCreatedBy()))
                .numberOfTeams(entity.getTeams().size())
                .numberOfMembers(entity.getMembers().size())
                .isOrgAccessBlocked(isOrgAccessBlocked)
                .orgMember(member != null ? orgMemberToDto(member) : null)
                .build();
    }

    public UserInfoDto userProfileToInfoDto(UserProfileEntity profileEntity) {
        return UserInfoDto.builder()
                .userId(profileEntity.getUserId())
                .email(profileEntity.getEmail())
                .firstName(profileEntity.getFirstName())
                .lastName(profileEntity.getLastName())
                .avatarId(profileEntity.getAvatar() != null ? profileEntity.getAvatar().getImageId() : null)
                .build();
    }

    public List<TeamDto> teamsToDto(List<TeamEntity> teams) {
        return teams != null
                ? teams.stream().map(this::teamToDto).collect(Collectors.toList())
                : new ArrayList<>();
    }

    public TeamDto teamToDto(TeamEntity team) {
        return TeamDto.builder()
                .id(team.getId())
                .organizationId(team.getOrganizationId())
                .teamId(team.getTeamId())
                .name(team.getName())
                .created(team.getCreated())
                .createdBy(modelMapper.map(team.getCreatedBy(), UserProfileDto.class))
                .members(teamMembersToDto(team.getMembers()))
                .totalMembers(team.getMembers().size())
                .build();
    }

    public List<OrganizationMemberDto> orgMembersToDto(List<OrganizationMemberEntity> members) {
        return members != null
                ? members.stream().map(this::orgMemberToDto).collect(Collectors.toList())
                : new ArrayList<>();
    }


    public OrganizationMemberDto orgMemberToDto(OrganizationMemberEntity orgMemberEntity) {
        return OrganizationMemberDto.builder()
                .id(orgMemberEntity.getId())
                .organizationId(orgMemberEntity.getOrganizationId())
                .role(orgMemberEntity.getRole())
                .joinDateTime(orgMemberEntity.getJoinDateTime())
                .status(orgMemberEntity.getStatus())
                .profile(modelMapper.map(orgMemberEntity.getProfile(), UserProfileDto.class))
                .invite(orgMemberEntity.getInvite() != null
                        ? inviteToDto(orgMemberEntity.getInvite())
                        : null)
                .changeHistory(memberHistoryToDto(orgMemberEntity.getChangeHistory()))
                .build();
    }

    public InviteDto inviteToDto(InviteEntity invite) {
        InviteDto inviteDto = modelMapper.map(invite, InviteDto.class);
        inviteDto.setIsExpired(invite.getExpiresAt().isBefore(LocalDateTime.now()));
        return inviteDto;
    }

    public InviteDto inviteToDto(InviteEntity invite, List<UserProfileDto> profiles) {
        UserProfileDto createdBy = profiles.stream()
                .filter(userProfileDto -> userProfileDto.getUserId().equals(invite.getCreateBy()))
                .findFirst().orElse(null);
        UserProfileDto createdFor = profiles.stream()
                .filter(userProfileDto -> userProfileDto.getUserId().equals(invite.getCreatedFor()) ||
                        userProfileDto.getEmail().equals(invite.getCreatedFor()))
                .findFirst().orElse(null);
        InviteDto inviteDto = modelMapper.map(invite, InviteDto.class);
        inviteDto.setIsExpired(invite.getExpiresAt().isBefore(LocalDateTime.now()));
        inviteDto.setCreateBy(createdBy);
        inviteDto.setCreatedFor(createdFor);
        inviteDto.setHasManagePermission(getUsername().equals(invite.getCreateBy()));
        inviteDto.setCurrentStatus(inviteDto.getIsExpired() ? InviteStatus.EXPIRED : invite.getCurrentStatus());
        return inviteDto;
    }

    public List<TeamMemberDto> teamMembersToDto(List<TeamMemberEntity> members) {
        return members != null
                ? members.stream().map(this::teamMemberToDto).collect(Collectors.toList())
                : new ArrayList<>();
    }


    public TeamMemberDto teamMemberToDto(TeamMemberEntity teamMemberEntity) {
        return TeamMemberDto.builder()
                .id(teamMemberEntity.getId())
                .role(teamMemberEntity.getRole())
                .joinDateTime(teamMemberEntity.getJoinDateTime())
                .badge(teamMemberEntity.getBadge())
                .status(teamMemberEntity.getStatus())
                .profile(modelMapper.map(teamMemberEntity.getProfile(), UserProfileDto.class))
                .changeHistory(memberHistoryToDto(teamMemberEntity.getChangeHistory()))
                .build();
    }


    public List<OrganizationHistoryDto> orgHistoryToDto(List<OrganizationHistory> history) {
        return history != null
                ? history.stream().map(this::orgHistoryToDto).collect(Collectors.toList())
                : new ArrayList<>();
    }


    public OrganizationHistoryDto orgHistoryToDto(OrganizationHistory organizationHistory) {
        return modelMapper.map(organizationHistory, OrganizationHistoryDto.class);
    }


    public List<MemberHistoryDto> memberHistoryToDto(List<MemberHistory> history) {
        return history != null
                ? history.stream().map(this::memberHistoryToDto).collect(Collectors.toList())
                : new ArrayList<>();
    }


    public MemberHistoryDto memberHistoryToDto(MemberHistory memberHistory) {
        return modelMapper.map(memberHistory, MemberHistoryDto.class);
    }

}
