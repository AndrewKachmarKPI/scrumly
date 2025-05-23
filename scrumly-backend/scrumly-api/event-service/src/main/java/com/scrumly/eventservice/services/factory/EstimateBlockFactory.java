package com.scrumly.eventservice.services.factory;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.domain.blocks.estimate.EstimateBlockEntity;
import com.scrumly.eventservice.domain.blocks.estimate.EstimateScaleEntity;
import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.dto.blocks.estimate.EstimateBlockDto;
import com.scrumly.eventservice.dto.requests.CreateActivityBlockRQ;
import com.scrumly.eventservice.dto.requests.estimate.CreateEstimateBlockRQ;
import com.scrumly.eventservice.repository.EstimateBlockRepository;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EstimateBlockFactory implements ActivityBlockFactory {
    private final EstimateBlockRepository estimateBlockRepository;

    @Override
    public ActivityBlockEntity createActivityBlock(CreateActivityBlockRQ createRQ) {
        if (!(createRQ instanceof CreateEstimateBlockRQ rq)) {
            throw new ServiceErrorException("Failed to create estimate block");
        }
        EstimateBlockEntity block = EstimateBlockEntity.builder()
                .blockId(UUID.randomUUID().toString())
                .type(rq.getType())
                .name(rq.getName())
                .description(rq.getDescription())
                .isMandatory(rq.getIsMandatory())
                .estimateMethod(rq.getEstimateMethod())
                .scale(EstimateScaleEntity.builder()
                        .scale(rq.getCreateScaleRQ().getScale())
                        .name(rq.getCreateScaleRQ().getName())
                        .build())
                .build();

        return estimateBlockRepository.save(block);
    }

    @Override
    public ActivityBlockEntity updateActivityBlock(String blockId, ActivityBlockDto dto) {
        if (!(dto instanceof EstimateBlockDto blockDto)) {
            throw new ServiceErrorException("Failed to create activity block");
        }
        EstimateBlockEntity block = (EstimateBlockEntity) findActivityBlock(blockId);
        if (blockDto.getName() != null && !Objects.equals(blockDto.getName(), block.getName())) {
            block.setName(blockDto.getName());
        }
        if (blockDto.getDescription() != null && !Objects.equals(blockDto.getDescription(), block.getDescription())) {
            block.setDescription(blockDto.getDescription());
        }
        if (blockDto.getIsMandatory() != null && !Objects.equals(blockDto.getIsMandatory(), block.getIsMandatory())) {
            block.setIsMandatory(blockDto.getIsMandatory());
        }

        if (blockDto.getEstimateMethod() != null && !Objects.equals(blockDto.getEstimateMethod(), block.getEstimateMethod())) {
            block.setEstimateMethod(blockDto.getEstimateMethod());
        }
        if (blockDto.getScale() != null) {
            block.setScale(block.getScale().toBuilder()
                    .name(blockDto.getScale().getName())
                    .scale(blockDto.getScale().getScale())
                    .build());
        }
        return estimateBlockRepository.save(block);
    }

    @Override
    public void deleteActivityBlock(String blockId) {
        estimateBlockRepository.deleteByBlockId(blockId);

    }

    @Override
    public ActivityBlockEntity findActivityBlock(String blockId) {
        return estimateBlockRepository.findByBlockId(blockId)
                .orElseThrow(() -> new EntityNotFoundException("Block is not found"));
    }

    @Override
    public ActivityBlockEntity copyActivityBlock(String blockId) {
        EstimateBlockEntity block = (EstimateBlockEntity) findActivityBlock(blockId);
        EstimateBlockEntity newBlock = new EstimateBlockEntity(block);
        newBlock.setId(null);
        newBlock.setBlockId(UUID.randomUUID().toString());
        return estimateBlockRepository.save(newBlock);
    }
}
