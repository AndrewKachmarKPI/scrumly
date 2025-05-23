package com.scrumly.eventservice.repository;

import com.scrumly.eventservice.domain.ActivityTemplateEntity;
import com.scrumly.eventservice.domain.ActivityTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivityTypeRepository extends JpaRepository<ActivityTypeEntity, Long>, JpaSpecificationExecutor<ActivityTypeEntity> {
    Boolean existsByType(String type);

    Optional<ActivityTypeEntity> findByType(String type);
}
