package com.scrumly.userservice.userservice.services;

import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationMemberDto;
import com.scrumly.userservice.userservice.enums.organization.OrganizationMemberRole;

public interface OrganizationMemberService {
    OrganizationMemberDto findMyOrganizationMemberAccount(String orgId, String username);

    PageDto<OrganizationMemberDto> findOrganizationMembers(String orgId, SearchQuery searchQuery);

    OrganizationMemberDto changeMemberRole(String orgId, String username, OrganizationMemberRole memberRole);

    OrganizationMemberDto blockMember(String orgId, String username);
    OrganizationMemberDto activateMember(String orgId, String username);

    void removeMember(String orgId, String username);
}
