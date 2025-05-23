package com.scrumly.eventservice.repository;

import com.scrumly.eventservice.domain.blocks.estimate.EstimateBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstimateBlockRepository extends JpaRepository<EstimateBlockEntity, Long> {
    void deleteByBlockId(String blockId);
    Optional<EstimateBlockEntity> findByBlockId(String blockId);
}
