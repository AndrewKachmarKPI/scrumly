package com.scrumly.userservice.userservice.services.impls;

import com.scrumly.exceptions.enums.ServiceErrorCode;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.specification.*;
import com.scrumly.userservice.userservice.domain.members.MemberHistory;
import com.scrumly.userservice.userservice.domain.organization.OrganizationEntity;
import com.scrumly.userservice.userservice.domain.organization.OrganizationMemberEntity;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationMemberDto;
import com.scrumly.userservice.userservice.dto.service.user.OrganizationConnectionDto;
import com.scrumly.userservice.userservice.enums.members.MemberChangeAction;
import com.scrumly.userservice.userservice.enums.members.MemberStatus;
import com.scrumly.userservice.userservice.enums.organization.OrganizationMemberRole;
import com.scrumly.userservice.userservice.mappers.BusinessMapper;
import com.scrumly.userservice.userservice.repository.InviteRepository;
import com.scrumly.userservice.userservice.repository.OrganizationMemberRepository;
import com.scrumly.userservice.userservice.repository.OrganizationRepository;
import com.scrumly.userservice.userservice.services.OrganizationMemberService;
import com.scrumly.userservice.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.scrumly.userservice.userservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class OrganizationMemberServiceImpl implements OrganizationMemberService {
    private final BusinessMapper businessMapper;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final InviteRepository inviteRepository;
    private final UserService userService;

    @Override
    public OrganizationMemberDto findMyOrganizationMemberAccount(String orgId, String username) {
        OrganizationMemberEntity organizationMember = organizationMemberRepository.findByProfile_UserIdAndOrganizationId(username, orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_MEMBER_NOTFOUND));
        return businessMapper.orgMemberToDto(organizationMember);
    }

    @Override
    public PageDto<OrganizationMemberDto> findOrganizationMembers(String orgId, SearchQuery searchQuery) {
        OrganizationEntity organization = organizationRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_NOTFOUND));
        searchQuery.appendSearchFilter(new SearchFilter("organizationId", SearchOperators.EQUALS, CompareOption.AND, organization.getOrganizationId()));
        Specification<OrganizationMemberEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        Page<OrganizationMemberEntity> page = organizationMemberRepository.findAll(specification, pageable);
        return GeneralSpecification.getPageResponse(page, businessMapper::orgMemberToDto);
    }

    @Override
    @Transactional
    public OrganizationMemberDto changeMemberRole(String orgId, String username, OrganizationMemberRole memberRole) {
        String performedBy = getUsername();
        OrganizationMemberEntity performedByMember = organizationMemberRepository.findByProfile_UserIdAndOrganizationId(performedBy, orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_MEMBER_NOTFOUND));
        if (performedBy.equals(username)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Cannot promote yourself, contact organization admin!");
        }

        if (!hasMemberActionPermission(orgId, performedBy)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Cannot manage organization members!");
        }

        OrganizationMemberEntity member = organizationMemberRepository.findByProfile_UserIdAndOrganizationId(username, orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_MEMBER_NOTFOUND));

        if (member.getRole().equals(OrganizationMemberRole.ORGANIZATION_ADMIN)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Cannot change admin!");
        }
        if (member.getRole().equals(memberRole)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Member already have specified role!");
        }
        if (!member.getStatus().equals(MemberStatus.ACTIVE)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Member is not active!");
        }

        if (performedByMember.getRole().equals(memberRole)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "You cannot modify fellow role user!");
        }

        member.setRole(memberRole);
        member.getChangeHistory()
                .add(MemberHistory.builder()
                        .dateTime(LocalDateTime.now())
                        .performedBy(performedBy)
                        .newStatus(MemberStatus.ACTIVE)
                        .changeAction(MemberChangeAction.MEMBER_ROLE_CHANGED)
                        .build());

        member = organizationMemberRepository.save(member);
        return businessMapper.orgMemberToDto(member);
    }

    @Override
    @Transactional
    public OrganizationMemberDto blockMember(String orgId, String username) {
        String performedBy = getUsername();
        if (performedBy.equals(username)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Cannot block yourself, contact organization admin!");
        }
        if (!hasMemberActionPermission(orgId, performedBy)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Cannot manage organization members!");
        }

        OrganizationMemberEntity member = organizationMemberRepository.findByProfile_UserIdAndOrganizationId(username, orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_MEMBER_NOTFOUND));

        if (member.getRole().equals(OrganizationMemberRole.ORGANIZATION_ADMIN)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Cannot block admin!");
        }
        if (!member.getStatus().equals(MemberStatus.ACTIVE)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Member cannot be blocked!");
        }

        member.setStatus(MemberStatus.BLOCKED);
        member.getChangeHistory()
                .add(MemberHistory.builder()
                        .dateTime(LocalDateTime.now())
                        .performedBy(performedBy)
                        .newStatus(MemberStatus.BLOCKED)
                        .changeAction(MemberChangeAction.MEMBER_BLOCKED)
                        .build());

        member = organizationMemberRepository.save(member);
        userService.changeOrganizationConnection(member.getProfile().getUserId(), OrganizationConnectionDto.builder()
                .dateConnected(LocalDateTime.now())
                .organizationId(orgId)
                .isActive(false)
                .build());
        return businessMapper.orgMemberToDto(member);
    }

    @Override
    @Transactional
    public OrganizationMemberDto activateMember(String orgId, String username) {
        String performedBy = getUsername();
        if (performedBy.equals(username)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Cannot activate yourself, contact organization admin!");
        }
        if (!hasMemberActionPermission(orgId, performedBy)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Cannot manage organization members!");
        }

        OrganizationMemberEntity member = organizationMemberRepository.findByProfile_UserIdAndOrganizationId(username, orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_MEMBER_NOTFOUND));
        if (member.getRole().equals(OrganizationMemberRole.ORGANIZATION_ADMIN)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Cannot activate admin!");
        }
        if (!member.getStatus().equals(MemberStatus.BLOCKED)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Member cannot be activated!");
        }

        member.setStatus(MemberStatus.ACTIVE);
        member.getChangeHistory()
                .add(MemberHistory.builder()
                        .dateTime(LocalDateTime.now())
                        .performedBy(performedBy)
                        .newStatus(MemberStatus.ACTIVE)
                        .changeAction(MemberChangeAction.MEMBER_ACTIVATED)
                        .build());

        member = organizationMemberRepository.save(member);
        userService.changeOrganizationConnection(member.getProfile().getUserId(), OrganizationConnectionDto.builder()
                .dateConnected(LocalDateTime.now())
                .organizationId(orgId)
                .isActive(true)
                .build());
        return businessMapper.orgMemberToDto(member);
    }

    @Override
    @Transactional
    public void removeMember(String orgId, String username) {
        String performedBy = getUsername();
        if (performedBy.equals(username)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Cannot remove yourself, contact organization admin!");
        }
        if (!hasMemberActionPermission(orgId, performedBy)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Cannot manage organization members!");
        }

        OrganizationMemberEntity member = organizationMemberRepository.findByProfile_UserIdAndOrganizationId(username, orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_MEMBER_NOTFOUND));
        if (member.getRole().equals(OrganizationMemberRole.ORGANIZATION_ADMIN)) {
            throw new ServiceErrorException(ServiceErrorCode.MEMBER_ACTION_ERROR, "Cannot remove admin!");
        }

        inviteRepository.delete(member.getInvite());

        OrganizationEntity organization = organizationRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_NOTFOUND));
        organization.getMembers()
                .removeIf(mem -> mem.getProfile().getUserId().equals(username));
        organizationRepository.save(organization);
        organizationMemberRepository.delete(member);

        userService.removeOrganizationConnection(member.getProfile().getUserId(), orgId);
    }

    private boolean hasMemberActionPermission(String orgId, String performer) {
        OrganizationMemberEntity performerMember = organizationMemberRepository.findByProfile_UserIdAndOrganizationId(performer, orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_MEMBER_NOTFOUND));
        return performerMember.getRole().equals(OrganizationMemberRole.ORGANIZATION_ADMIN) ||
                performerMember.getRole().equals(OrganizationMemberRole.ORGANIZATION_LEAD);
    }
}
