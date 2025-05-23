package com.scrumly.eventservice.repository;

import com.scrumly.eventservice.domain.workspace.WorkspaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceEntityRepository extends JpaRepository<WorkspaceEntity, Long> {
    WorkspaceEntity findByWorkspaceId(String workspaceId);
}
