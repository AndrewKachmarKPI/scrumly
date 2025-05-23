package com.scrumly.userservice.userservice.services;

import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import com.scrumly.userservice.userservice.domain.organization.OrganizationEntity;
import com.scrumly.userservice.userservice.domain.organization.OrganizationMemberEntity;
import com.scrumly.userservice.userservice.dto.requests.CreateOrganizationRQ;
import com.scrumly.userservice.userservice.dto.requests.OrganizationInfoRQ;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationDto;
import com.scrumly.userservice.userservice.dto.service.organization.OrganizationInfoDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OrganizationService {
    OrganizationInfoDto createOrganization(CreateOrganizationRQ rq, MultipartFile logo);

    OrganizationInfoDto updateOrganization(String orgId, OrganizationInfoRQ organizationInfoRQ, MultipartFile logo);

    OrganizationInfoDto archiveOrganization(String orgId);

    OrganizationInfoDto deleteOrganization(String orgId);

    PageDto<OrganizationInfoDto> findOrganizations(SearchQuery searchQuery);

    PageDto<OrganizationInfoDto> findOrganizationsByUsername(SearchQuery searchQuery, String username);

    OrganizationDto findOrganization(String orgId);

    OrganizationInfoDto findOrganizationInfo(String orgId);

    List<OrganizationInfoDto> findOrganizationInfoList(List<String> orgIds);

    OrganizationEntity findOrganizationOrThrow(String orgId);

    boolean checkIfExistsInOrganization(String ordId, List<String> userIds);

    OrganizationMemberEntity getOrganizationMember(String orgId, String username);
}
