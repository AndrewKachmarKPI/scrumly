package com.scrumly.userservice.userservice.services.impls;

import com.scrumly.exceptions.enums.ServiceErrorCode;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.specification.*;
import com.scrumly.userservice.userservice.domain.members.MemberHistory;
import com.scrumly.userservice.userservice.domain.organization.OrganizationEntity;
import com.scrumly.userservice.userservice.domain.organization.OrganizationHistory;
import com.scrumly.userservice.userservice.domain.organization.OrganizationMemberEntity;
import com.scrumly.userservice.userservice.domain.team.TeamEntity;
import com.scrumly.userservice.userservice.domain.user.OrganizationConnectionEntity;
import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import com.scrumly.userservice.userservice.dto.requests.CreateOrganizationRQ;
import com.scrumly.userservice.userservice.dto.requests.CreateTeamRQ;
import com.scrumly.userservice.userservice.dto.requests.InviteMembersRQ;
import com.scrumly.userservice.userservice.dto.requests.OrganizationInfoRQ;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationDto;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationInfoDto;
import com.scrumly.userservice.userservice.dto.service.user.OrganizationConnectionDto;
import com.scrumly.userservice.userservice.enums.UserRole;
import com.scrumly.userservice.userservice.enums.members.MemberChangeAction;
import com.scrumly.userservice.userservice.enums.members.MemberStatus;
import com.scrumly.userservice.userservice.enums.organization.OrganizationChangeAction;
import com.scrumly.userservice.userservice.enums.organization.OrganizationMemberRole;
import com.scrumly.userservice.userservice.enums.organization.OrganizationStatus;
import com.scrumly.userservice.userservice.mappers.BusinessMapper;
import com.scrumly.userservice.userservice.repository.OrganizationRepository;
import com.scrumly.userservice.userservice.repository.TeamRepository;
import com.scrumly.userservice.userservice.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.scrumly.userservice.userservice.utils.SecurityUtils.getUsername;
import static com.scrumly.userservice.userservice.utils.SecurityUtils.hasRole;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    private final ImageService imageService;
    private final UserService userService;
    private final BusinessMapper businessMapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationInviteService organizationInviteService;

    @Override
    @Transactional
    public OrganizationInfoDto createOrganization(CreateOrganizationRQ rq, MultipartFile logo) {
        OrganizationEntity organization = organizationRepository.findByName(rq.getOrganizationName());
        if (organization != null) {
            throw new DuplicateEntityException(ServiceErrorCode.DUPLICATE_ORGANIZATION);
        }

        String organizationId = UUID.randomUUID().toString();
        LocalDateTime dateTimeCreated = LocalDateTime.now();
        UserProfileEntity creatorProfile = userService.getUserProfileOrThrow(getUsername());
        List<OrganizationMemberEntity> creatorMembers = new ArrayList<>();
        List<MemberHistory> creatorMemberHistory = new ArrayList<>();
        creatorMemberHistory.add(MemberHistory.builder()
                .dateTime(dateTimeCreated)
                .performedBy(getUsername())
                .newStatus(MemberStatus.ACTIVE)
                .changeAction(MemberChangeAction.MEMBER_CREATED)
                .build());
        creatorMembers.add(OrganizationMemberEntity.builder()
                .organizationId(organizationId)
                .role(OrganizationMemberRole.ORGANIZATION_ADMIN)
                .joinDateTime(dateTimeCreated)
                .status(MemberStatus.ACTIVE)
                .profile(creatorProfile)
                .changeHistory(creatorMemberHistory)
                .build());

        List<OrganizationHistory> histories = new ArrayList<>();
        histories.add(OrganizationHistory.builder()
                .dateTime(dateTimeCreated)
                .performedBy(getUsername())
                .newStatus(OrganizationStatus.ACTIVE)
                .changeAction(OrganizationChangeAction.CREATED)
                .build());
        organization = OrganizationEntity.builder()
                .organizationId(organizationId)
                .name(rq.getOrganizationName())
                .logo(imageService.saveImage(logo))
                .status(OrganizationStatus.ACTIVE)
                .created(LocalDateTime.now())
                .createdBy(creatorProfile)
                .members(creatorMembers)
                .teams(new ArrayList<>())
                .isActive(true)
                .changeHistory(histories)
                .build();

        organization = organizationRepository.save(organization);

        userService.changeOrganizationConnection(getUsername(), OrganizationConnectionDto.builder()
                .dateConnected(LocalDateTime.now())
                .organizationId(organizationId)
                .isActive(true)
                .build());

        if (rq.getInviteMembers() != null && !rq.getInviteMembers().isEmpty()) {
            organizationInviteService.inviteOrganizationMembers(organization.getOrganizationId(), InviteMembersRQ.builder()
                    .usernames(rq.getInviteMembers())
                    .build());
        }

        return businessMapper.organizationToInfoDto(organization);
    }

    @Override
    public OrganizationInfoDto updateOrganization(String orgId, OrganizationInfoRQ organizationInfoRQ, MultipartFile logo) {
        OrganizationEntity organization = findOrganizationOrThrow(orgId);
        OrganizationMemberEntity organizationMember = getOrganizationMember(orgId, getUsername());

        if (!organizationMember.getRole().equals(OrganizationMemberRole.ORGANIZATION_ADMIN)) {
            throw new ServiceErrorException(ServiceErrorCode.ORGANIZATION_MEMBER_INSUFFICIENT_PERMISSION);
        }

        if (organizationInfoRQ.getName() != null) {
            organization.setName(organizationInfoRQ.getName());
        }
        if (organizationInfoRQ.getAbout() != null) {
            organization.setAbout(organizationInfoRQ.getAbout());
        }
        if (logo != null) {
            if (organization.getLogo() != null) {
                imageService.deleteImage(organization.getLogo().getImageId());
            }
            organization.setLogo(imageService.saveImage(logo));
        }
        if (organizationInfoRQ.isRemoveLogo()) {
            if (organization.getLogo() != null) {
                imageService.deleteImage(organization.getLogo().getImageId());
                organization.setLogo(null);
            }
        }
        if (organizationInfoRQ.getStatus() != null && hasRole(UserRole.SUPER_ADMIN.name())) {
            organization.setStatus(organizationInfoRQ.getStatus());
            organization.getChangeHistory().add(
                    OrganizationHistory.builder()
                            .dateTime(LocalDateTime.now())
                            .performedBy(getUsername())
                            .newStatus(organizationInfoRQ.getStatus())
                            .changeAction(OrganizationChangeAction.UPDATED)
                            .build()
            );
        }
        organization = organizationRepository.save(organization);
        return businessMapper.organizationToInfoDto(organization);
    }

    @Override
    @Transactional
    public OrganizationInfoDto archiveOrganization(String orgId) {
        OrganizationEntity organization = findOrganizationOrThrow(orgId);
        if (organization.getStatus().equals(OrganizationStatus.ARCHIVED)) {
            throw new ServiceErrorException(ServiceErrorCode.ORGANIZATION_NOTFOUND, "Organization is already archived!");
        }

        OrganizationMemberEntity organizationMember = getOrganizationMember(orgId, getUsername());
        if (!organizationMember.getRole().equals(OrganizationMemberRole.ORGANIZATION_ADMIN)) {
            throw new ServiceErrorException(ServiceErrorCode.ORGANIZATION_MEMBER_INSUFFICIENT_PERMISSION);
        }

        organization.setIsActive(false);
        organization.getChangeHistory().add(
                OrganizationHistory.builder()
                        .dateTime(LocalDateTime.now())
                        .performedBy(getUsername())
                        .newStatus(OrganizationStatus.ARCHIVED)
                        .previousStatus(organization.getStatus())
                        .changeAction(OrganizationChangeAction.ORG_ARCHIVED)
                        .build()
        );
        organization.setStatus(OrganizationStatus.ARCHIVED);

        organization = organizationRepository.save(organization);
        return businessMapper.organizationToInfoDto(organization);
    }

    @Override
    @Transactional
    public OrganizationInfoDto deleteOrganization(String orgId) {
        OrganizationEntity organization = findOrganizationOrThrow(orgId);
        if (!organization.getStatus().equals(OrganizationStatus.ARCHIVED)) {
            throw new ServiceErrorException(ServiceErrorCode.ORGANIZATION_NOTFOUND, "Organization is not archived!");
        }

        OrganizationMemberEntity organizationMember = getOrganizationMember(orgId, getUsername());
        if (!organizationMember.getRole().equals(OrganizationMemberRole.ORGANIZATION_ADMIN)) {
            throw new ServiceErrorException(ServiceErrorCode.ORGANIZATION_MEMBER_INSUFFICIENT_PERMISSION);
        }

        organizationRepository.delete(organization);
        userService.removeAllOrganizationConnection(orgId);
        return businessMapper.organizationToInfoDto(organization);
    }

    @Override
    public OrganizationDto findOrganization(String organizationId) {
        return businessMapper.organizationToDto(findOrganizationOrThrow(organizationId));
    }

    @Override
    public OrganizationInfoDto findOrganizationInfo(String orgId) {
        return businessMapper.organizationToInfoDto(findOrganizationOrThrow(orgId));
    }

    @Override
    public List<OrganizationInfoDto> findOrganizationInfoList(List<String> orgIds) {
        List<OrganizationInfoDto> infos = new ArrayList<>();
        for (String orgId : orgIds) {
            try {
                infos.add(findOrganizationInfo(orgId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return infos;
    }

    @Override
    public OrganizationEntity findOrganizationOrThrow(String organizationId) {
        return organizationRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_NOTFOUND));
    }

    @Override
    public PageDto<OrganizationInfoDto> findOrganizations(SearchQuery searchQuery) {
        Specification<OrganizationEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        Page<OrganizationEntity> page = organizationRepository.findAll(specification, pageable);
        return GeneralSpecification.getPageResponse(page, businessMapper::organizationToInfoDto);
    }

    @Override
    public PageDto<OrganizationInfoDto> findOrganizationsByUsername(SearchQuery searchQuery, String username) {
        UserProfileEntity userProfileEntity = userService.getUserProfileOrThrow(username);
        List<String> organizationIds = userProfileEntity.getConnectedOrganizations()
                .stream().map(OrganizationConnectionEntity::getOrganizationId)
                .collect(Collectors.toList());
        if (organizationIds.isEmpty()) {
            return new PageDto<>();
        }
        searchQuery.appendSearchFilter(new SearchFilter("organizationId", SearchOperators.IN, CompareOption.AND, organizationIds));
        return findOrganizations(searchQuery);
    }


    @Override
    public boolean checkIfExistsInOrganization(String orgId, List<String> userIds) {
        OrganizationEntity organization = findOrganizationOrThrow(orgId);
        return organization.getMembers().stream()
                .anyMatch(member -> member.getProfile() != null && member.getInvite() != null &&
                        userIds.contains(member.getProfile().getUserId()));
    }

    @Override
    public OrganizationMemberEntity getOrganizationMember(String orgId, String username) {
        OrganizationEntity organization = organizationRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_NOTFOUND));
        return organization.getMembers().stream()
                .filter(member -> member.getProfile().getUserId().equals(username))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ServiceErrorCode.ORGANIZATION_MEMBER_NOTFOUND));
    }

}
