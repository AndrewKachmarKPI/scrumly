package com.scrumly.repository.backlog;

import com.scrumly.domain.backlog.IssueEntity;
import com.scrumly.domain.backlog.IssueStatusEntity;
import com.scrumly.domain.backlog.IssueTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueStatusRepository extends JpaRepository<IssueStatusEntity, Long> {
    boolean existsByStatusAndBacklogId(String status, String backlogId);

    Optional<IssueStatusEntity> findByBacklogIdAndStatus(String backlogId, String status);

    List<IssueStatusEntity> findAllByBacklogId(String backlogId);
}
