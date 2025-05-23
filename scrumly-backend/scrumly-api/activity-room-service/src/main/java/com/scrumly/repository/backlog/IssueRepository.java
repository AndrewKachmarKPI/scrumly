package com.scrumly.repository.backlog;

import com.scrumly.domain.backlog.BacklogEntity;
import com.scrumly.domain.backlog.IssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<IssueEntity, Long>, JpaSpecificationExecutor<IssueEntity> {
    Optional<IssueEntity> findByIssueKey(String issueKey);
    List<IssueEntity> findAllByIssueKeyIn(List<String> issueKey);
}
