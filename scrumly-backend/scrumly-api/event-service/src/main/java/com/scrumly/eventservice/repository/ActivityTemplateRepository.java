package com.scrumly.eventservice.repository;

import com.scrumly.eventservice.domain.ActivityTemplateEntity;
import com.scrumly.eventservice.enums.ActivityScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityTemplateRepository extends JpaRepository<ActivityTemplateEntity, Long>, JpaSpecificationExecutor<ActivityTemplateEntity> {
    Boolean existsByName(String name);
    Boolean existsByNameAndOwner_OwnerId(String name, String ownerId);

    Optional<ActivityTemplateEntity> findByName(String name);

    Optional<ActivityTemplateEntity> findByTemplateId(String templateId);
    Optional<ActivityTemplateEntity> findByTemplateIdAndOwner_OwnerId(String templateId, String ownerId);

    List<ActivityTemplateEntity> findAllByOwner_Scope(ActivityScope scope);
    List<ActivityTemplateEntity> findAllByOwner_ScopeAndOwner_OwnerId(ActivityScope scope, String ownerId);

    ActivityTemplateEntity findByNameAndOwner_OwnerId(String name, String ownerId);
}
