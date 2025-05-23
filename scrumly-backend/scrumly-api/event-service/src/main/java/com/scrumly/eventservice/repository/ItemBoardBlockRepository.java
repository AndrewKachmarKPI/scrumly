package com.scrumly.eventservice.repository;

import com.scrumly.eventservice.domain.blocks.itemsBoard.ItemsBoardBlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemBoardBlockRepository extends JpaRepository<ItemsBoardBlockEntity, Long> {
    Optional<ItemsBoardBlockEntity> findByBlockId(String blockId);

    void deleteByBlockId(String blockId);
}
