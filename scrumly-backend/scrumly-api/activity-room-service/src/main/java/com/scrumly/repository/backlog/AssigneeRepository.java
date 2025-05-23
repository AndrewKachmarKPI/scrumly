package com.scrumly.repository.backlog;

import com.scrumly.domain.backlog.AssigneeEntity;
import com.scrumly.domain.backlog.IssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssigneeRepository extends JpaRepository<AssigneeEntity, Long> {
    AssigneeEntity findByUserId(String userId);
}
