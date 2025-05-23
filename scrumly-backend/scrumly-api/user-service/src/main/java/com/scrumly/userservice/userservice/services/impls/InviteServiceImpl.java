package com.scrumly.userservice.userservice.services.impls;

import com.scrumly.exceptions.enums.ServiceErrorCode;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.specification.*;
import com.scrumly.userservice.userservice.domain.invites.InviteEntity;
import com.scrumly.userservice.userservice.domain.invites.InviteHistoryEntity;
import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import com.scrumly.userservice.userservice.dto.service.invite.InviteDto;
import com.scrumly.userservice.userservice.dto.service.user.UserProfileDto;
import com.scrumly.userservice.userservice.enums.invite.InviteChangeAction;
import com.scrumly.userservice.userservice.enums.invite.InvitePermission;
import com.scrumly.userservice.userservice.enums.invite.InviteStatus;
import com.scrumly.userservice.userservice.enums.invite.InviteType;
import com.scrumly.userservice.userservice.mappers.BusinessMapper;
import com.scrumly.userservice.userservice.repository.InviteRepository;
import com.scrumly.userservice.userservice.repository.OrganizationMemberRepository;
import com.scrumly.userservice.userservice.services.InviteService;
import com.scrumly.userservice.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.scrumly.userservice.userservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class InviteServiceImpl implements InviteService {
    private final BusinessMapper businessMapper;
    private final InviteRepository inviteRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserService userService;

    @Value("${invite.expiration-days}")
    private String inviteExpirationDays;
    @Value("${invite.base-url}")
    private String inviteBaseUrl;

    @Override
    public InviteEntity createNewInvite(String connectingId, String username) {
        InviteEntity invite = inviteRepository
                .findByConnectingIdAndCreatedForAndInviteTypeAndCurrentStatusIn(
                        connectingId, username, InviteType.ORGANIZATION,
                        List.of(InviteStatus.NEW, InviteStatus.RESENT,
                                InviteStatus.EXPIRED, InviteStatus.ACCEPTED)
                );
        UserProfileEntity createForProfile = userService.getUserProfileOrThrow(username);
        if (invite != null) {
            throw new DuplicateEntityException(ServiceErrorCode.DUPLICATE_INVITE);
        }
        LocalDateTime created = LocalDateTime.now();
        LocalDateTime expired = created.plusDays(Long.parseLong(inviteExpirationDays));
        String inviteId = UUID.randomUUID().toString();
        invite = InviteEntity.builder()
                .inviteId(inviteId)
                .inviteUrl(getInviteUrl(inviteId))
                .connectingId(connectingId)
                .inviteType(InviteType.ORGANIZATION)
                .currentStatus(InviteStatus.NEW)
                .createBy(getUsername())
                .createdFor(createForProfile.getUserId())
                .created(created)
                .expiresAt(expired)
                .changeLog(List.of(
                        InviteHistoryEntity.builder()
                                .dateTime(created)
                                .performedBy(getUsername())
                                .newStatus(InviteStatus.NEW)
                                .changeAction(InviteChangeAction.INVITATION_CREATED)
                                .build()
                ))
                .build();
        return inviteRepository.save(invite);
    }

    @Override
    public InviteDto findInvite(String inviteId) {
        if (!checkInvitePermissions(inviteId, InvitePermission.READ)) {
            throw new ServiceErrorException(ServiceErrorCode.INVITE_INSUFFICIENT_PERMISSION);
        }
        checkIfInviteExpired(inviteId, InviteChangeAction.INVITATION_VIEWED);

        InviteEntity invite = findInviteOrThrow(inviteId);
        Set<String> inviteMembers = getInviteUsername(List.of(invite));
        List<UserProfileDto> inviteMembersProfiles = new ArrayList<>(userService.findUserProfiles(inviteMembers));
        return businessMapper.inviteToDto(invite, inviteMembersProfiles);
    }

    @Override
    public PageDto<InviteDto> findInvitesBySearchQuery(SearchQuery searchQuery) {
        Specification<InviteEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        Page<InviteEntity> page = inviteRepository.findAll(specification, pageable);

        Set<String> usernames = getInviteUsername(page.getContent());
        List<UserProfileDto> profiles = new ArrayList<>(userService.findUserProfiles(usernames));
        return GeneralSpecification.getPageResponse(page, invite -> businessMapper.inviteToDto(invite, profiles));
    }

    @Override
    public PageDto<InviteDto> findUserPendingInvites(String username, SearchQuery searchQuery) {
        searchQuery.appendSearchFilter(new SearchFilter("currentStatus", CustomSearchOperators.IS_INVITE_STATUS_EQUAL, CompareOption.OR, InviteStatus.NEW.toString()));
        searchQuery.appendSearchFilter(new SearchFilter("currentStatus", CustomSearchOperators.IS_INVITE_STATUS_EQUAL, CompareOption.OR, InviteStatus.RESENT.toString()));
        searchQuery.appendSearchFilter(new SearchFilter("createdFor", SearchOperators.EQUALS, CompareOption.AND, username));
        return findInvitesBySearchQuery(searchQuery);
    }

    @Override
    public boolean checkInvitePermissions(String inviteId, InvitePermission permission) {
        InviteEntity invite = findInviteOrThrow(inviteId);
        String username = getUsername();
        if (permission.equals(InvitePermission.READ)) {
            return invite.getCreatedFor().equals(username) ||
                    invite.getCreateBy().equals(username);
        } else if (permission.equals(InvitePermission.RESEND)) {
            return invite.getCreateBy().equals(username);
        } else if (permission.equals(InvitePermission.ACCEPT)) {
            return invite.getCreatedFor().equals(username);
        } else if (permission.equals(InvitePermission.REJECT)) {
            return invite.getCreatedFor().equals(username);
        } else if (permission.equals(InvitePermission.REVOKE)) {
            return invite.getCreateBy().equals(username);
        } else if (permission.equals(InvitePermission.DELETE)) {
            return invite.getCreateBy().equals(username);
        }
        return false;
    }

    @Override
    public void resendInvite(String inviteId) {
        InviteEntity invite = findInviteOrThrow(inviteId);
        if (!checkInvitePermissions(inviteId, InvitePermission.RESEND)) {
            throw new ServiceErrorException(ServiceErrorCode.INVITE_INSUFFICIENT_PERMISSION);
        }
        if (invite.getCurrentStatus().equals(InviteStatus.EXPIRED) ||
                invite.getCurrentStatus().equals(InviteStatus.REJECTED)) {
            LocalDateTime expired = LocalDateTime.now().plusDays(Long.parseLong(inviteExpirationDays));
            invite.setExpiresAt(expired);
            invite.setCurrentStatus(InviteStatus.RESENT);
            invite.getChangeLog().add(InviteHistoryEntity.builder()
                    .dateTime(LocalDateTime.now())
                    .performedBy(getUsername())
                    .newStatus(InviteStatus.RESENT)
                    .changeAction(InviteChangeAction.INVITATION_CREATED)
                    .build());
            inviteRepository.save(invite);
        } else {
            throw new ServiceErrorException(ServiceErrorCode.INVITE_INSUFFICIENT_STATUS);
        }
    }

    @Override
    @Transactional
    public void acceptInvite(String inviteId) {
        if (!checkInvitePermissions(inviteId, InvitePermission.ACCEPT)) {
            throw new ServiceErrorException(ServiceErrorCode.INVITE_INSUFFICIENT_PERMISSION);
        }
        checkIfInviteExpired(inviteId, InviteChangeAction.INVITATION_ACCEPTED);

        InviteEntity invite = findInviteOrThrow(inviteId);
        if (invite.getCurrentStatus().equals(InviteStatus.NEW) ||
                invite.getCurrentStatus().equals(InviteStatus.RESENT)) {
            invite = invite.toBuilder()
                    .currentStatus(InviteStatus.ACCEPTED)
                    .accepted(LocalDateTime.now())
                    .build();
            invite.getChangeLog().add(InviteHistoryEntity.builder()
                    .dateTime(LocalDateTime.now())
                    .performedBy(getUsername())
                    .newStatus(InviteStatus.ACCEPTED)
                    .changeAction(InviteChangeAction.INVITATION_ACCEPTED)
                    .build());
            inviteRepository.save(invite);
        } else {
            throw new ServiceErrorException(ServiceErrorCode.INVITE_INSUFFICIENT_STATUS);
        }
    }


    @Override
    public void rejectInvite(String inviteId) {
        if (!checkInvitePermissions(inviteId, InvitePermission.REJECT)) {
            throw new ServiceErrorException(ServiceErrorCode.INVITE_INSUFFICIENT_PERMISSION);
        }
        checkIfInviteExpired(inviteId, InviteChangeAction.INVITATION_ACCEPTED);

        InviteEntity invite = findInviteOrThrow(inviteId);
        if (invite.getCurrentStatus().equals(InviteStatus.NEW) ||
                invite.getCurrentStatus().equals(InviteStatus.RESENT)) {
            invite = invite.toBuilder()
                    .currentStatus(InviteStatus.REJECTED)
                    .accepted(LocalDateTime.now())
                    .build();
            invite.getChangeLog().add(InviteHistoryEntity.builder()
                    .dateTime(LocalDateTime.now())
                    .performedBy(getUsername())
                    .newStatus(InviteStatus.REJECTED)
                    .changeAction(InviteChangeAction.INVITATION_REJECTED)
                    .build());
            inviteRepository.save(invite);
        } else {
            throw new ServiceErrorException(ServiceErrorCode.INVITE_INSUFFICIENT_STATUS);
        }
    }

    @Override
    public void revokeInvite(String inviteId) {
        if (!checkInvitePermissions(inviteId, InvitePermission.REVOKE)) {
            throw new ServiceErrorException(ServiceErrorCode.INVITE_INSUFFICIENT_PERMISSION);
        }
        InviteEntity invite = findInviteOrThrow(inviteId);
        if (invite.getCurrentStatus().equals(InviteStatus.NEW) ||
                invite.getCurrentStatus().equals(InviteStatus.RESENT) ||
                invite.getCurrentStatus().equals(InviteStatus.EXPIRED)) {
            invite = invite.toBuilder()
                    .currentStatus(InviteStatus.REVOKED)
                    .accepted(LocalDateTime.now())
                    .build();
            invite.getChangeLog().add(InviteHistoryEntity.builder()
                    .dateTime(LocalDateTime.now())
                    .performedBy(getUsername())
                    .newStatus(InviteStatus.REVOKED)
                    .changeAction(InviteChangeAction.INVITATION_REVOKED)
                    .build());
            inviteRepository.save(invite);
        } else {
            throw new ServiceErrorException(ServiceErrorCode.INVITE_INSUFFICIENT_STATUS);
        }
    }

    @Override
    public void deleteInvite(String inviteId) {
        if (!checkInvitePermissions(inviteId, InvitePermission.DELETE)) {
            throw new ServiceErrorException(ServiceErrorCode.INVITE_INSUFFICIENT_PERMISSION);
        }
        InviteEntity invite = findInviteOrThrow(inviteId);
        if (invite.getCurrentStatus().equals(InviteStatus.ACCEPTED)) {
            throw new ServiceErrorException(ServiceErrorCode.INVITE_INSUFFICIENT_STATUS);
        }
        inviteRepository.delete(invite);
    }

    @Override
    public void deleteOldInvites(List<String> inviteIds) {
        for (String inviteId : inviteIds) {
            InviteEntity invite = findInviteOrNull(inviteId);
            if (invite != null) {
                inviteRepository.delete(invite);
            }
        }
    }

    @Override
    public void checkIfInviteExpired(String inviteId, InviteChangeAction action) {
        InviteEntity invite = findInviteOrThrow(inviteId);
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setCurrentStatus(InviteStatus.EXPIRED);
            invite.getChangeLog().add(InviteHistoryEntity.builder()
                    .dateTime(LocalDateTime.now())
                    .performedBy(getUsername())
                    .newStatus(InviteStatus.EXPIRED)
                    .changeAction(action)
                    .build());
            inviteRepository.save(invite);
            throw new ServiceErrorException(ServiceErrorCode.INVITE_INSUFFICIENT_PERMISSION);
        }
    }

    private static Set<String> getInviteUsername(List<InviteEntity> page) {
        Set<String> usernames = new HashSet<>();
        usernames.addAll(page.stream()
                .map(InviteEntity::getCreateBy)
                .toList());
        usernames.addAll(page.stream()
                .map(InviteEntity::getCreatedFor)
                .toList());
        return usernames;
    }

    private InviteEntity findInviteOrThrow(String inviteId) {
        InviteEntity invite = inviteRepository.findByInviteId(inviteId);
        if (invite == null) {
            throw new DuplicateEntityException(ServiceErrorCode.DUPLICATE_INVITE);
        }
        return invite;
    }

    private InviteEntity findInviteOrNull(String inviteId) {
        return inviteRepository.findByInviteId(inviteId);
    }

    private String getInviteUrl(String inviteId) {
        return inviteBaseUrl + "/" + inviteId;
    }
}
