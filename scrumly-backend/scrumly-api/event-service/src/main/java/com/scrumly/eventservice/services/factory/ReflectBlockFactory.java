package com.scrumly.eventservice.services.factory;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.domain.blocks.question.QuestionBlockEntity;
import com.scrumly.eventservice.domain.blocks.question.QuestionEntity;
import com.scrumly.eventservice.domain.blocks.reflect.ReflectBlockEntity;
import com.scrumly.eventservice.domain.blocks.reflect.ReflectColumnEntity;
import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.dto.blocks.question.QuestionBlockDto;
import com.scrumly.eventservice.dto.blocks.reflect.ReflectBlockDto;
import com.scrumly.eventservice.dto.blocks.reflect.ReflectColumnDto;
import com.scrumly.eventservice.dto.requests.CreateActivityBlockRQ;
import com.scrumly.eventservice.dto.requests.question.CreateQuestionBlockRQ;
import com.scrumly.eventservice.dto.requests.reflect.CreateReflectBlockRQ;
import com.scrumly.eventservice.enums.ActivityBlockType;
import com.scrumly.eventservice.repository.QuestionBlockRepository;
import com.scrumly.eventservice.repository.ReflectBlockRepository;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReflectBlockFactory implements ActivityBlockFactory {
    private final ReflectBlockRepository reflectBlockRepository;

    @Override
    public ActivityBlockEntity createActivityBlock(CreateActivityBlockRQ createRq) {
        if (!(createRq instanceof CreateReflectBlockRQ rq)) {
            throw new ServiceErrorException("Failed to create reflect block");
        }

        List<ReflectColumnEntity> reflectColumns = rq.getReflectColumns().stream()
                .map(columnRQ -> ReflectColumnEntity.builder()
                        .columnOrder(columnRQ.getColumnOrder())
                        .title(columnRQ.getTitle())
                        .color(columnRQ.getColor())
                        .instruction(columnRQ.getInstruction())
                        .build())
                .toList();

        ReflectBlockEntity block = ReflectBlockEntity.builder()
                .blockId(UUID.randomUUID().toString())
                .type(rq.getType())
                .name(rq.getName())
                .description(rq.getDescription())
                .isMandatory(rq.getIsMandatory())
                .reflectColumns(reflectColumns)
                .maxReflectionsPerColumnPerUser(rq.getMaxReflectionsPerColumnPerUser())
                .timePerColumn(rq.getTimePerColumn())
                .reflectTimeLimit(rq.getReflectTimeLimit())
                .build();
        return reflectBlockRepository.save(block);

    }

    @Override
    public ActivityBlockEntity updateActivityBlock(String blockId, ActivityBlockDto dto) {
        if (!(dto instanceof ReflectBlockDto blockDto)) {
            throw new ServiceErrorException("Failed to create activity block");
        }
        ReflectBlockEntity block = (ReflectBlockEntity) findActivityBlock(blockId);

        if (blockDto.getName() != null && !Objects.equals(blockDto.getName(), block.getName())) {
            block.setName(blockDto.getName());
        }
        if (blockDto.getDescription() != null && !Objects.equals(blockDto.getDescription(), block.getDescription())) {
            block.setDescription(blockDto.getDescription());
        }
        if (blockDto.getIsMandatory() != null && !Objects.equals(blockDto.getIsMandatory(), block.getIsMandatory())) {
            block.setIsMandatory(blockDto.getIsMandatory());
        }
        if (blockDto.getMaxReflectionsPerColumnPerUser() != null &&
                !Objects.equals(blockDto.getMaxReflectionsPerColumnPerUser(), block.getMaxReflectionsPerColumnPerUser())) {
            block.setMaxReflectionsPerColumnPerUser(blockDto.getMaxReflectionsPerColumnPerUser());
        }
        if (blockDto.getTimePerColumn() != null &&
                !Objects.equals(blockDto.getTimePerColumn(), block.getTimePerColumn())) {
            block.setTimePerColumn(blockDto.getTimePerColumn());
        }
        if (blockDto.getReflectTimeLimit() != null &&
                !Objects.equals(blockDto.getReflectTimeLimit(), block.getReflectTimeLimit())) {
            block.setReflectTimeLimit(blockDto.getReflectTimeLimit());
        }

        if (blockDto.getReflectColumns() != null && !blockDto.getReflectColumns().isEmpty()) {
            Map<Long, ReflectColumnEntity> existingColumnsMap = block.getReflectColumns().stream()
                    .collect(Collectors.toMap(ReflectColumnEntity::getId, Function.identity()));

            List<ReflectColumnEntity> updatedColumns = new ArrayList<>();
            for (ReflectColumnDto newColumnDto : blockDto.getReflectColumns()) {
                ReflectColumnEntity columnEntity = new ReflectColumnEntity();
                if (newColumnDto.getId() != null && existingColumnsMap.containsKey(newColumnDto.getId())) {
                    columnEntity = existingColumnsMap.get(newColumnDto.getId());
                }
                columnEntity.setColumnOrder(newColumnDto.getColumnOrder());
                columnEntity.setTitle(newColumnDto.getTitle());
                columnEntity.setColor(newColumnDto.getColor());
                columnEntity.setInstruction(newColumnDto.getInstruction());
                updatedColumns.add(columnEntity);
            }
            block.getReflectColumns().clear();
            block.getReflectColumns().addAll(updatedColumns);
        }
        return reflectBlockRepository.save(block);
    }


    @Override
    @Transactional
    public void deleteActivityBlock(String blockId) {
        reflectBlockRepository.deleteByBlockId(blockId);
    }

    @Override
    public ActivityBlockEntity copyActivityBlock(String blockId) {
        ReflectBlockEntity block = (ReflectBlockEntity) findActivityBlock(blockId);
        ReflectBlockEntity newBlock = new ReflectBlockEntity(block);
        newBlock.setId(null);
        newBlock.setBlockId(UUID.randomUUID().toString());
        return reflectBlockRepository.save(newBlock);
    }

    @Override
    public ActivityBlockEntity findActivityBlock(String blockId) {
        return reflectBlockRepository.findByBlockId(blockId)
                .orElseThrow(() -> new EntityNotFoundException("Block is not found"));
    }
}
