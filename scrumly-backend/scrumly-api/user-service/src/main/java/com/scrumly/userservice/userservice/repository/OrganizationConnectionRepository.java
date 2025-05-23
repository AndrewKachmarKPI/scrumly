package com.scrumly.userservice.userservice.repository;

import com.scrumly.userservice.userservice.domain.user.OrganizationConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationConnectionRepository extends JpaRepository<OrganizationConnectionEntity, Long> {
}
