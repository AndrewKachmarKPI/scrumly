package com.scrumly.userservice.userservice.repository;

import com.scrumly.userservice.userservice.domain.organization.OrganizationMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMemberEntity, Long>, JpaSpecificationExecutor<OrganizationMemberEntity> {
    OrganizationMemberEntity findByInvite_InviteId(String inviteId);
    Optional<OrganizationMemberEntity> findByProfile_UserIdAndOrganizationId(String userId, String orgId);
}
