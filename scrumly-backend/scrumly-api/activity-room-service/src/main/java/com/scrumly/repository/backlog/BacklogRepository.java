package com.scrumly.repository.backlog;

import com.scrumly.domain.backlog.BacklogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BacklogRepository extends JpaRepository<BacklogEntity, Long> {
    boolean existsByTeamId(String teamId);

    List<BacklogEntity> findAllByTeamId(String teamId);


    BacklogEntity findByBacklogId(String backlogId);
}
