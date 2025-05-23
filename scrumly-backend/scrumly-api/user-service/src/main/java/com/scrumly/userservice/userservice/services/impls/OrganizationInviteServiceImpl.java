package com.scrumly.userservice.userservice.services.impls;

import com.scrumly.exceptions.enums.ServiceErrorCode;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.specification.*;
import com.scrumly.userservice.userservice.domain.invites.InviteEntity;
import com.scrumly.userservice.userservice.domain.members.MemberHistory;
import com.scrumly.userservice.userservice.domain.organization.OrganizationEntity;
import com.scrumly.userservice.userservice.domain.organization.OrganizationMemberEntity;
import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import com.scrumly.userservice.userservice.dto.requests.InviteMembersRQ;
import com.scrumly.userservice.userservice.dto.service.invite.InviteDto;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationInfoDto;
import com.scrumly.userservice.userservice.dto.service.user.OrganizationConnectionDto;
import com.scrumly.userservice.userservice.enums.invite.InviteStatus;
import com.scrumly.userservice.userservice.enums.invite.InviteType;
import com.scrumly.userservice.userservice.enums.members.MemberChangeAction;
import com.scrumly.userservice.userservice.enums.members.MemberStatus;
import com.scrumly.userservice.userservice.enums.organization.OrganizationMemberRole;
import com.scrumly.userservice.userservice.mappers.BusinessMapper;
import com.scrumly.userservice.userservice.repository.OrganizationMemberRepository;
import com.scrumly.userservice.userservice.repository.OrganizationRepository;
import com.scrumly.userservice.userservice.services.InviteService;
import com.scrumly.userservice.userservice.services.OrganizationInviteService;
import com.scrumly.userservice.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.scrumly.userservice.userservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class OrganizationInviteServiceImpl implements OrganizationInviteService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final InviteService inviteService;
    private final UserService userService;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional
    public List<String> inviteOrganizationMembers(String orgId, InviteMembersRQ inviteMembersRQ) {
        OrganizationEntity organization = organizationRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_NOTFOUND));
        UserProfileEntity creatorProfile = userService.getUserProfileOrThrow(getUsername());
        List<String> membersUserIds = organization.getMembers().stream()
                .filter(member -> member.getProfile() != null)
                .map(member -> member.getProfile().getUserId())
                .toList();
        if (organization.getMembers().stream()
                .anyMatch(member -> member.getInvite() != null && inviteMembersRQ.getUsernames()
                        .contains(member.getProfile().getUserId()))) {
            throw new DuplicateEntityException(ServiceErrorCode.DUPLICATE_MEMBER);
        }

        if (inviteMembersRQ.getUsernames().contains(creatorProfile.getUserId())) {
            throw new DuplicateEntityException(ServiceErrorCode.INVALID_INVITE);
        }

        reInviteUsers(orgId, inviteMembersRQ, membersUserIds, organization);

        List<String> newUsers = inviteMembersRQ.getUsernames().stream()
                .filter(email -> !membersUserIds.contains(email))
                .toList();
        LocalDateTime joinDateTime = LocalDateTime.now();
        List<OrganizationMemberEntity> members = newUsers.stream()
                .map(username -> {
                    List<MemberHistory> creatorMemberHistory = new ArrayList<>();
                    creatorMemberHistory.add(MemberHistory.builder()
                            .dateTime(joinDateTime)
                            .performedBy(getUsername())
                            .newStatus(MemberStatus.PENDING)
                            .changeAction(MemberChangeAction.MEMBER_INVITED)
                            .build());
                    return OrganizationMemberEntity.builder()
                            .organizationId(organization.getOrganizationId())
                            .role(OrganizationMemberRole.MEMBER)
                            .joinDateTime(joinDateTime)
                            .status(MemberStatus.PENDING)
                            .profile(userService.getUserProfileOrThrow(username))
                            .invite(createOrganizationInvite(orgId, username))
                            .changeHistory(creatorMemberHistory)
                            .build();
                })
                .toList();

        organization.getMembers().addAll(members);
        organizationRepository.save(organization);

        //TODO SEND EMAIL WITH INVITATION
        return newUsers;
    }

    private void reInviteUsers(String orgId, InviteMembersRQ inviteMembersRQ, List<String> memberUserIds, OrganizationEntity organization) {
        List<String> reInviteUsers = inviteMembersRQ.getUsernames().stream()
                .filter(memberUserIds::contains)
                .toList();
        organization.getMembers().stream()
                .filter(member -> member.getProfile() != null &&
                        reInviteUsers.contains(member.getProfile().getUserId()))
                .forEach(member -> member.setInvite(createOrganizationInvite(orgId, member.getProfile().getUserId())));
        //TODO RESEND EMAIL
    }

    private InviteEntity createOrganizationInvite(String orgId, String username) {
        return inviteService.createNewInvite(orgId, username);
    }

    @Override
    public PageDto<InviteDto> findOrganizationInvites(String orgId, SearchQuery searchQuery) {
        OrganizationEntity organization = organizationRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_NOTFOUND));
        searchQuery.appendSearchFilter(new SearchFilter("connectingId", SearchOperators.EQUALS, CompareOption.AND, organization.getOrganizationId()));
        searchQuery.appendSearchFilter(new SearchFilter("inviteType", CustomSearchOperators.IS_INVITE_TYPE_EQUAL, CompareOption.AND, InviteType.ORGANIZATION.toString()));
        return inviteService.findInvitesBySearchQuery(searchQuery);
    }


    @Override
    public PageDto<InviteDto> findUserOrganizationInvites(String username, SearchQuery searchQuery) {
        searchQuery.appendSearchFilter(new SearchFilter("inviteType", CustomSearchOperators.IS_INVITE_TYPE_EQUAL, CompareOption.AND, InviteType.ORGANIZATION.toString()));
        PageDto<InviteDto> invites = inviteService.findUserPendingInvites(username, searchQuery);
        Set<String> connectingIds = invites.getData().stream()
                .map(InviteDto::getConnectingId)
                .collect(Collectors.toSet());
        Map<String, OrganizationInfoDto> organizationInfoDtoMap = connectingIds.stream()
                .map(organizationRepository::getByOrganizationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(OrganizationEntity::getOrganizationId, businessMapper::organizationToInfoDto));

        List<InviteDto> activeOrgInvites = invites.getData().stream()
                .filter(inviteDto -> !inviteDto.getIsExpired() &&
                        organizationInfoDtoMap.containsKey(inviteDto.getConnectingId()))
                .toList();

        activeOrgInvites.forEach(inviteDto -> inviteDto.setOrgInfoDto(organizationInfoDtoMap.get(inviteDto.getConnectingId())));
        invites.setData(activeOrgInvites);
        return invites;
    }


    @Override
    public InviteDto findInvite(String orgId, String inviteId) {
        InviteDto inviteDto = inviteService.findInvite(inviteId);
        OrganizationEntity organization = organizationRepository
                .findByOrganizationId(inviteDto.getConnectingId())
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_NOTFOUND));
        inviteDto.setOrgInfoDto(businessMapper.organizationToInfoDto(organization));
        return inviteDto;
    }

    @Override
    public void resendInvite(String orgId, String inviteId) {
        inviteService.resendInvite(inviteId);
    }

    @Override
    @Transactional
    public void acceptInvite(String orgId, String inviteId) {
        OrganizationEntity organization = organizationRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_NOTFOUND));
        OrganizationMemberEntity member = organization.getMembers()
                .stream().filter(mem -> mem.getInvite() != null && mem.getInvite().getInviteId().equals(inviteId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_MEMBER_NOTFOUND));
        member.setStatus(MemberStatus.ACTIVE);
        member.setJoinDateTime(LocalDateTime.now());
        member.getChangeHistory().add(MemberHistory.builder()
                .newStatus(MemberStatus.ACTIVE)
                .previousStatus(MemberStatus.PENDING)
                .performedBy(getUsername())
                .changeAction(MemberChangeAction.MEMBER_JOINED)
                .dateTime(LocalDateTime.now())
                .build());
        organizationMemberRepository.save(member);
        inviteService.acceptInvite(inviteId);
        userService.changeOrganizationConnection(member.getProfile().getUserId(), OrganizationConnectionDto.builder()
                .dateConnected(LocalDateTime.now())
                .organizationId(orgId)
                .isActive(true)
                .build());
    }

    @Override
    public void rejectInvite(String orgId, String inviteId) {
        inviteService.rejectInvite(inviteId);
    }

    @Override
    public void revokeInvite(String orgId, String inviteId) {
        inviteService.revokeInvite(inviteId);
    }

    @Override
    @Transactional
    public void deleteInvite(String orgId, String inviteId) {
        OrganizationEntity organization = organizationRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_NOTFOUND));
        OrganizationMemberEntity member = organization.getMembers()
                .stream().filter(mem -> mem.getInvite() != null && mem.getInvite().getInviteId().equals(inviteId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_MEMBER_NOTFOUND));
        member.setInvite(null);
        organizationRepository.save(organization);
        inviteService.deleteInvite(inviteId);
    }
}
