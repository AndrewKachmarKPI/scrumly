package com.scrumly.eventservice.repository;

import com.scrumly.eventservice.domain.blocks.question.QuestionBlockEntity;
import com.scrumly.eventservice.domain.blocks.question.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionBlockRepository extends JpaRepository<QuestionBlockEntity, String> {
    Optional<QuestionBlockEntity> findByBlockId(String blockId);
    void deleteByBlockId(String blockId);
}

