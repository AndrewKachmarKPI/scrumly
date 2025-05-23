package com.scrumly.repository.backlog;

import com.scrumly.domain.backlog.IssueTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueTypeRepository extends JpaRepository<IssueTypeEntity, Long> {
    boolean existsByBacklogIdAndType(String backlogId, String type);

    Optional<IssueTypeEntity> findByBacklogIdAndType(String backlogId, String type);

    List<IssueTypeEntity> findAllByBacklogId(String backlogId);
}
