package com.scrumly.integrationservice.repository.jira;

import com.scrumly.integrationservice.domain.jira.JiraConnectionResourcesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JiraConnectionResourcesRepository extends JpaRepository<JiraConnectionResourcesEntity, Long> {
    JiraConnectionResourcesEntity findByConnectionId(String connectionId);
}
