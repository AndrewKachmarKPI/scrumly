package com.scrumly.eventservice.repository;

import com.scrumly.eventservice.domain.blocks.reflect.ReflectBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReflectBlockRepository extends JpaRepository<ReflectBlockEntity, Long> {
    Optional<ReflectBlockEntity> findByBlockId(String blockId);
    void deleteByBlockId(String blockId);
}
