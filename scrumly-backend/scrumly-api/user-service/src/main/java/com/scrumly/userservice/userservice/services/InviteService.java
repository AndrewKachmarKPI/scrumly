package com.scrumly.userservice.userservice.services;

import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import com.scrumly.userservice.userservice.domain.invites.InviteEntity;
import com.scrumly.userservice.userservice.dto.service.invite.InviteDto;
import com.scrumly.userservice.userservice.enums.invite.InviteChangeAction;
import com.scrumly.userservice.userservice.enums.invite.InvitePermission;

import java.util.List;

public interface InviteService {
    InviteEntity createNewInvite(String connectingId, String username);

    InviteDto findInvite(String inviteId);

    PageDto<InviteDto> findInvitesBySearchQuery(SearchQuery searchQuery);

    PageDto<InviteDto> findUserPendingInvites(String username, SearchQuery searchQuery);

    boolean checkInvitePermissions(String inviteId, InvitePermission permission);

    void resendInvite(String inviteId);

    void acceptInvite(String inviteId);

    void rejectInvite(String inviteId);

    void revokeInvite(String inviteId);

    void deleteInvite(String inviteId);

    void deleteOldInvites(List<String> inviteId);

    void checkIfInviteExpired(String inviteId, InviteChangeAction action);

}
