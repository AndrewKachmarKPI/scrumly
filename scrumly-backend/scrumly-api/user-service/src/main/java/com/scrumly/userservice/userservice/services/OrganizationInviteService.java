package com.scrumly.userservice.userservice.services;

import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import com.scrumly.userservice.userservice.dto.requests.InviteMembersRQ;
import com.scrumly.userservice.userservice.dto.service.invite.InviteDto;

import java.util.List;

public interface OrganizationInviteService {

    List<String> inviteOrganizationMembers(String orgId, InviteMembersRQ inviteMembersRQ);

    PageDto<InviteDto> findOrganizationInvites(String orgId, SearchQuery searchQuery);

    PageDto<InviteDto> findUserOrganizationInvites(String username, SearchQuery searchQuery);

    InviteDto findInvite(String orgId, String inviteId);

    void resendInvite(String orgId, String inviteId);

    void acceptInvite(String orgId, String inviteId);

    void rejectInvite(String orgId, String inviteId);

    void revokeInvite(String orgId, String inviteId);

    void deleteInvite(String orgId, String inviteId);
}
