package com.scrumly.userservice.userservice.repository;

import com.scrumly.userservice.userservice.domain.team.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Long>, JpaSpecificationExecutor<TeamEntity> {
    Optional<TeamEntity> findByTeamId(String  teamId);
    TeamEntity findByNameAndOrganizationId(String name, String orgId);

    Boolean existsByTeamIdAndOrganizationId(String teamId, String orgId);
    Boolean existsByTeamIdAndOrganizationIdAndName(String teamId, String orgId, String name);

    List<TeamEntity> findAllByOrganizationId(String orgId);
}
