package com.scrumly.eventservice.services.factory;

import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.domain.blocks.itemsBoard.ItemsBoardBlockEntity;
import com.scrumly.eventservice.domain.blocks.itemsBoard.ItemsBoardColumnEntity;
import com.scrumly.eventservice.domain.blocks.itemsBoard.ItemsBoardColumnStatusEntity;
import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.dto.blocks.itemsBoard.ItemsBoardBlockDto;
import com.scrumly.eventservice.dto.blocks.itemsBoard.ItemsBoardColumnDto;
import com.scrumly.eventservice.dto.blocks.itemsBoard.ItemsBoardColumnStatusDto;
import com.scrumly.eventservice.dto.requests.CreateActivityBlockRQ;
import com.scrumly.eventservice.dto.requests.itemsBoard.CreateItemsBoardColumnStatusRQ;
import com.scrumly.eventservice.dto.requests.itemsBoard.CreateItemsBoardRQ;
import com.scrumly.eventservice.repository.ItemBoardBlockRepository;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemsBoardBlockFactory implements ActivityBlockFactory {
    private final ItemBoardBlockRepository itemBoardBlockRepository;


    @Override
    public ActivityBlockEntity createActivityBlock(CreateActivityBlockRQ createRq) {
        if (!(createRq instanceof CreateItemsBoardRQ rq)) {
            throw new ServiceErrorException("Failed to create reflect block");
        }

        List<ItemsBoardColumnEntity> columns = rq.getBoardColumns().stream()
                .map(columnRQ -> ItemsBoardColumnEntity.builder()
                        .columnOrder(columnRQ.getColumnOrder())
                        .title(columnRQ.getTitle())
                        .instruction(columnRQ.getInstruction())
                        .maxItems(columnRQ.getMaxItems())
                        .color(columnRQ.getColor())
                        .statusMapping(columnRQ.getStatusMapping() != null ?
                                               new ArrayList<>(columnRQ.getStatusMapping().stream()
                                                                       .map(createItemsBoardColumnStatusRQ -> ItemsBoardColumnStatusEntity.builder()
                                                                               .statusId(createItemsBoardColumnStatusRQ.getStatusId())
                                                                               .status(createItemsBoardColumnStatusRQ.getStatus())
                                                                               .color(createItemsBoardColumnStatusRQ.getColor())
                                                                               .backlogId(createItemsBoardColumnStatusRQ.getBacklogId())
                                                                               .build())
                                                                       .toList()) : null)
                        .build())
                .toList();

        List<ItemsBoardColumnStatusEntity> doneStatuses = null;
        if (rq.getDoneStatuses() != null) {
            List<CreateItemsBoardColumnStatusRQ> doneStatusesRQ = new ArrayList<>(rq.getDoneStatuses());
            doneStatuses = new ArrayList<>(doneStatusesRQ.stream()
                                                   .map(createItemsBoardColumnStatusRQ -> ItemsBoardColumnStatusEntity.builder()
                                                           .statusId(createItemsBoardColumnStatusRQ.getStatusId())
                                                           .status(createItemsBoardColumnStatusRQ.getStatus())
                                                           .color(createItemsBoardColumnStatusRQ.getColor())
                                                           .backlogId(createItemsBoardColumnStatusRQ.getBacklogId())
                                                           .build())
                                                   .toList());
        }

        List<ItemsBoardColumnStatusEntity> inProgressStatuses = null;
        if (rq.getInProgressStatuses() != null) {
            List<CreateItemsBoardColumnStatusRQ> inProgressStatusesRQ = new ArrayList<>(rq.getInProgressStatuses());
            inProgressStatuses = new ArrayList<>(inProgressStatusesRQ.stream()
                                                         .map(createItemsBoardColumnStatusRQ -> ItemsBoardColumnStatusEntity.builder()
                                                                 .statusId(createItemsBoardColumnStatusRQ.getStatusId())
                                                                 .status(createItemsBoardColumnStatusRQ.getStatus())
                                                                 .color(createItemsBoardColumnStatusRQ.getColor())
                                                                 .backlogId(createItemsBoardColumnStatusRQ.getBacklogId())
                                                                 .build())
                                                         .toList());
        }

        ItemsBoardBlockEntity block = ItemsBoardBlockEntity.builder()
                .blockId(UUID.randomUUID().toString())
                .type(rq.getType())
                .name(rq.getName())
                .description(rq.getDescription())
                .isMandatory(rq.getIsMandatory())
                .boardColumns(columns)
                .maxItems(rq.getMaxItems())
                .doneStatuses(doneStatuses)
                .inProgressStatuses(inProgressStatuses)
                .build();
        return itemBoardBlockRepository.save(block);

    }

    @Override
    public ActivityBlockEntity updateActivityBlock(String blockId, ActivityBlockDto dto) {
        if (!(dto instanceof ItemsBoardBlockDto blockDto)) {
            throw new ServiceErrorException("Failed to create activity block");
        }
        ItemsBoardBlockEntity block = (ItemsBoardBlockEntity) findActivityBlock(blockId);

        if (blockDto.getName() != null && !Objects.equals(blockDto.getName(), block.getName())) {
            block.setName(blockDto.getName());
        }
        if (blockDto.getDescription() != null && !Objects.equals(blockDto.getDescription(), block.getDescription())) {
            block.setDescription(blockDto.getDescription());
        }
        if (blockDto.getIsMandatory() != null && !Objects.equals(blockDto.getIsMandatory(), block.getIsMandatory())) {
            block.setIsMandatory(blockDto.getIsMandatory());
        }
        if (blockDto.getMaxItems() != null &&
                !Objects.equals(blockDto.getMaxItems(), block.getMaxItems())) {
            block.setMaxItems(blockDto.getMaxItems());
        }

        if (blockDto.getBoardColumns() != null && !blockDto.getBoardColumns().isEmpty()) {
            Map<Long, ItemsBoardColumnEntity> existingColumnsMap = block.getBoardColumns().stream()
                    .collect(Collectors.toMap(ItemsBoardColumnEntity::getId, Function.identity()));

            List<ItemsBoardColumnEntity> updatedColumns = new ArrayList<>();
            for (ItemsBoardColumnDto newColumnDto : blockDto.getBoardColumns()) {
                ItemsBoardColumnEntity columnEntity = new ItemsBoardColumnEntity();
                if (newColumnDto.getId() != null && existingColumnsMap.containsKey(newColumnDto.getId())) {
                    columnEntity = existingColumnsMap.get(newColumnDto.getId());
                }
                columnEntity.setColumnOrder(newColumnDto.getColumnOrder());
                columnEntity.setTitle(newColumnDto.getTitle());
                columnEntity.setColor(newColumnDto.getColor());
                columnEntity.setInstruction(newColumnDto.getInstruction());
                columnEntity.setMaxItems(newColumnDto.getMaxItems());
                if (newColumnDto.getStatusMapping() != null) {
                    columnEntity.setStatusMapping(new ArrayList<>(newColumnDto.getStatusMapping().stream()
                                                                          .map(itemsBoardColumnStatusDto -> ItemsBoardColumnStatusEntity.builder()
                                                                                  .statusId(itemsBoardColumnStatusDto.getStatusId())
                                                                                  .status(itemsBoardColumnStatusDto.getStatus())
                                                                                  .color(itemsBoardColumnStatusDto.getColor())
                                                                                  .backlogId(itemsBoardColumnStatusDto.getBacklogId())
                                                                                  .build())
                                                                          .toList()));
                }
                updatedColumns.add(columnEntity);
            }
            block.getBoardColumns().clear();
            block.getBoardColumns().addAll(updatedColumns);
        }

        if (blockDto.getDoneStatuses() != null && !blockDto.getDoneStatuses().isEmpty()) {
            List<ItemsBoardColumnStatusEntity> doneStatuses = blockDto.getDoneStatuses().stream()
                    .map(createItemsBoardColumnStatusRQ -> ItemsBoardColumnStatusEntity.builder()
                            .id(createItemsBoardColumnStatusRQ.getId())
                            .statusId(createItemsBoardColumnStatusRQ.getStatusId())
                            .status(createItemsBoardColumnStatusRQ.getStatus())
                            .backlogId(createItemsBoardColumnStatusRQ.getBacklogId())
                            .color(createItemsBoardColumnStatusRQ.getColor())
                            .build())
                    .toList();
            List<ItemsBoardColumnStatusEntity> existingStatuses = block.getDoneStatuses();
            existingStatuses.removeIf(existingStatus ->
                                              doneStatuses.stream().noneMatch(newStatus ->
                                                                                      newStatus.getStatusId().equals(existingStatus.getStatusId()) &&
                                                                                              newStatus.getBacklogId().equals(existingStatus.getBacklogId())
                                              ));
            for (ItemsBoardColumnStatusEntity newStatus : doneStatuses) {
                boolean exists = existingStatuses.stream().anyMatch(existingStatus ->
                                                                            existingStatus.getStatusId().equals(newStatus.getStatusId()) &&
                                                                                    existingStatus.getBacklogId().equals(newStatus.getBacklogId())
                );
                if (!exists) {
                    existingStatuses.add(newStatus);
                }
            }
            block.setDoneStatuses(existingStatuses);
        }
        if (blockDto.getInProgressStatuses() != null && !blockDto.getInProgressStatuses().isEmpty()) {
            List<ItemsBoardColumnStatusEntity> inProgressStatuses = blockDto.getInProgressStatuses().stream()
                    .map(createItemsBoardColumnStatusRQ -> ItemsBoardColumnStatusEntity.builder()
                            .id(createItemsBoardColumnStatusRQ.getId())
                            .statusId(createItemsBoardColumnStatusRQ.getStatusId())
                            .status(createItemsBoardColumnStatusRQ.getStatus())
                            .backlogId(createItemsBoardColumnStatusRQ.getBacklogId())
                            .color(createItemsBoardColumnStatusRQ.getColor())
                            .build())
                    .toList();
            List<ItemsBoardColumnStatusEntity> existingStatuses = block.getInProgressStatuses();
            existingStatuses.removeIf(existingStatus ->
                                              inProgressStatuses.stream().noneMatch(newStatus ->
                                                                                            newStatus.getStatusId().equals(existingStatus.getStatusId()) &&
                                                                                                    newStatus.getBacklogId().equals(existingStatus.getBacklogId())
                                              ));
            for (ItemsBoardColumnStatusEntity newStatus : inProgressStatuses) {
                boolean exists = existingStatuses.stream().anyMatch(existingStatus ->
                                                                            existingStatus.getStatusId().equals(newStatus.getStatusId()) &&
                                                                                    existingStatus.getBacklogId().equals(newStatus.getBacklogId())
                );
                if (!exists) {
                    existingStatuses.add(newStatus);
                }
            }
            block.setInProgressStatuses(existingStatuses);
        }

        return itemBoardBlockRepository.save(block);
    }


    @Override
    @Transactional
    public void deleteActivityBlock(String blockId) {
        itemBoardBlockRepository.deleteByBlockId(blockId);
    }

    @Override
    public ActivityBlockEntity copyActivityBlock(String blockId) {
        ItemsBoardBlockEntity block = (ItemsBoardBlockEntity) findActivityBlock(blockId);
        ItemsBoardBlockEntity newBlock = new ItemsBoardBlockEntity(block);
        newBlock.setId(null);
        newBlock.setBlockId(UUID.randomUUID().toString());
        return itemBoardBlockRepository.save(newBlock);
    }

    @Override
    public ActivityBlockEntity findActivityBlock(String blockId) {
        return itemBoardBlockRepository.findByBlockId(blockId)
                .orElseThrow(() -> new EntityNotFoundException("Block is not found"));
    }
}
