package com.scrumly.userservice.userservice.repository;

import com.scrumly.userservice.userservice.domain.organization.OrganizationEntity;
import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long>, JpaSpecificationExecutor<OrganizationEntity> {
    OrganizationEntity findByName(String name);

    Optional<OrganizationEntity> findByOrganizationId(String organizationId);
    OrganizationEntity getByOrganizationId(String organizationId);
}
