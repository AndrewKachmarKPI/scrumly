package com.scrumly.eventservice.services.impl;

import com.scrumly.dto.ImageRequestDto;
import com.scrumly.eventservice.domain.ActivityBlockConfigEntity;
import com.scrumly.eventservice.domain.ActivityBlockEntity;
import com.scrumly.eventservice.domain.ActivityOwnerEntity;
import com.scrumly.eventservice.domain.ActivityTemplateEntity;
import com.scrumly.eventservice.dto.ActivityBlockConfigDto;
import com.scrumly.eventservice.dto.ActivityBlockDto;
import com.scrumly.eventservice.dto.ActivityTemplateDto;
import com.scrumly.eventservice.dto.ActivityTemplateGroupDto;
import com.scrumly.eventservice.dto.requests.CreateActivityBlockConfigRQ;
import com.scrumly.eventservice.dto.requests.CreateActivityTemplateRQ;
import com.scrumly.eventservice.enums.ActivityScope;
import com.scrumly.eventservice.feign.UserServiceFeignClient;
import com.scrumly.eventservice.mapper.BusinessMapper;
import com.scrumly.eventservice.repository.ActivityTemplateRepository;
import com.scrumly.eventservice.services.ActivityBlockService;
import com.scrumly.eventservice.services.ActivityTemplateService;
import com.scrumly.eventservice.services.ActivityTypeService;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.specification.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.scrumly.eventservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class ActivityTemplateServiceImpl implements ActivityTemplateService {
    private final ActivityTemplateRepository templateRepository;
    private final ActivityTypeService activityTypeService;
    private final ActivityBlockService activityBlockService;
    private final UserServiceFeignClient userServiceFeignClient;
    private final BusinessMapper businessMapper;


    @Override
    @Transactional
    public ActivityTemplateDto createActivityTemplate(CreateActivityTemplateRQ createRQ, MultipartFile previewImg) {
        String imageId = null;
        ActivityTemplateDto templateDto = null;
        if (previewImg != null) {
            try {
                imageId = userServiceFeignClient.saveImage(ImageRequestDto.builder()
                        .fileName(previewImg.getOriginalFilename())
                        .image(previewImg.getBytes())
                        .type(previewImg.getContentType())
                        .build()).getBody();
                createRQ.setPreviewImageId(imageId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            templateDto = createActivityTemplate(createRQ);
        } catch (Exception e) {
            e.printStackTrace();
            if (imageId != null) {
                userServiceFeignClient.deleteImageById(imageId);
            }
            throw new ServiceErrorException(e);
        }
        return templateDto;
    }

    @Override
    public ActivityTemplateDto createActivityTemplate(CreateActivityTemplateRQ createRQ) {
        ActivityTemplateEntity template = saveActivityTemplate(createRQ);
        return businessMapper.activityTemplateToDto(template);
    }


    @Override
    public ActivityTemplateDto updateActivityTemplate(String templateId,
                                                      ActivityTemplateDto dto,
                                                      MultipartFile previewImg) {
        ActivityTemplateEntity template = templateRepository.findByTemplateId(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template with such id is not found"));

        String imageId = null;
        String oldImageId = null;
        ActivityTemplateDto templateDto = null;
        if (template.getPreviewImageId() != null) {
            oldImageId = template.getPreviewImageId();
        }
        if (previewImg != null) {
            try {
                imageId = userServiceFeignClient.saveImage(ImageRequestDto.builder()
                        .fileName(previewImg.getOriginalFilename())
                        .image(previewImg.getBytes())
                        .type(previewImg.getContentType())
                        .build()).getBody();
                dto.setPreviewImageId(imageId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            templateDto = updateActivityTemplate(templateId, dto);
        } catch (Exception e) {
            e.printStackTrace();
            if (imageId != null) {
                userServiceFeignClient.deleteImageById(imageId);
            }
            throw new ServiceErrorException(e);
        }
        if (oldImageId != null) {
            userServiceFeignClient.deleteImageById(oldImageId);
        }
        return templateDto;
    }

    @Override
    @Transactional
    public ActivityTemplateDto updateActivityTemplate(String templateId, ActivityTemplateDto dto) {
        ActivityTemplateEntity template = templateRepository.findByTemplateId(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template with such id is not found"));

        if (!template.getOwner().getOwnerId().equals(dto.getOwner().getOwnerId())) {
            throw new ServiceErrorException("Only owner can update template");
        }

        template = updateTemplateDetails(dto, template);
        template = updateTemplateBlocks(dto, template);
        return businessMapper.activityTemplateToDto(template);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ActivityTemplateEntity updateTemplateDetails(ActivityTemplateDto dto, ActivityTemplateEntity template) {
        if (dto.getName() != null && !dto.getName().equals(template.getName())) {
            if (templateRepository.existsByNameAndOwner_OwnerId(dto.getName(), dto.getOwner().getOwnerId())) {
                throw new DuplicateEntityException("Template with such name already exists!");
            }
            template.setName(dto.getName());
        }
        if (dto.getDescription() != null && !dto.getDescription().equals(template.getDescription())) {
            template.setDescription(dto.getDescription());
        }
        if (dto.getPreviewImageId() != null && !dto.getPreviewImageId().equals(template.getPreviewImageId())) {
            template.setPreviewImageId(dto.getPreviewImageId());
        }
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            template.setTags(dto.getTags());
        }
        if (dto.getType() != null && !dto.getType().getType().equals(template.getType().getType())) {
            template.setType(activityTypeService.getEntityByType(dto.getType().getType()));
        }
        return templateRepository.save(template);
    }

    public ActivityTemplateEntity updateTemplateBlocks(ActivityTemplateDto dto, ActivityTemplateEntity template) {

        for (ActivityBlockConfigDto block : dto.getBlocks()) {
            ActivityBlockConfigEntity blockConfigEntity = template.getBlocks().stream()
                    .filter(blockConfig -> blockConfig.getBlockId().equals(block.getBlockId()))
                    .findAny().orElse(null);
            if (blockConfigEntity != null) {
                blockConfigEntity.setBlockOrder(block.getBlockOrder());
                if (block.getBlockDto() != null) {
                    activityBlockService.updateActivityBlock(block.getBlockId(), block.getBlockDto());
                }
            }
        }
        template.getBlocks().removeIf(blockConfig -> dto.getBlocks().stream()
                .noneMatch(blockConfigDto -> blockConfig.getBlockId().equals(blockConfigDto.getBlockId())));

        if (dto.getNewBlocks() != null) {
            for (CreateActivityBlockConfigRQ newBlock : dto.getNewBlocks()) {
                ActivityBlockEntity block = activityBlockService.createActivityBlock(newBlock.getBlock());
                ActivityBlockConfigEntity blockConfigEntity = ActivityBlockConfigEntity.builder()
                        .blockOrder(newBlock.getOrder())
                        .isOptional(newBlock.getIsOptional())
                        .blockId(block.getBlockId())
                        .blockType(block.getType())
                        .build();
                template.getBlocks().add(blockConfigEntity);
            }
        }
        return templateRepository.save(template);
    }

    @Override
    @Transactional
    public ActivityTemplateDto copyActivityTemplate(String templateId, String ownerId) {
        ActivityTemplateEntity copiedTemplate = null;
        String imageId = null;
        try {
            ActivityTemplateEntity template = templateRepository.findByTemplateId(templateId)
                    .orElseThrow(() -> new EntityNotFoundException("Template with such id is not found"));
            copiedTemplate = new ActivityTemplateEntity(template);
            copiedTemplate.setTemplateId(UUID.randomUUID().toString());
            copiedTemplate.setName("(COPY) of " + copiedTemplate.getName());
            copiedTemplate.setOwner(ActivityOwnerEntity.builder()
                    .ownerId(ownerId)
                    .scope(ActivityScope.PRIVATE)
                    .dateTimeCreated(LocalDateTime.now())
                    .createdById(getUsername())
                    .build());

            if (copiedTemplate.getPreviewImageId() != null) {
                imageId = userServiceFeignClient.copyImage(copiedTemplate.getPreviewImageId()).getBody();
                copiedTemplate.setPreviewImageId(imageId);
            }

            List<ActivityBlockConfigEntity> blockConfigs = new ArrayList<>();
            for (ActivityBlockConfigEntity block : template.getBlocks()) {
                ActivityBlockEntity copiedBlock = activityBlockService.copyActivityBlock(block.getBlockId(), block.getBlockType());
                blockConfigs.add(ActivityBlockConfigEntity.builder()
                        .blockOrder(block.getBlockOrder())
                        .blockType(block.getBlockType())
                        .isOptional(block.isOptional())
                        .blockId(copiedBlock.getBlockId())
                        .build());
            }
            copiedTemplate.setBlocks(blockConfigs);
            copiedTemplate = templateRepository.save(copiedTemplate);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            if (imageId != null) {
                userServiceFeignClient.deleteImageById(imageId);
            }
            throw new ServiceErrorException(e);
        }
        return businessMapper.activityTemplateToDto(copiedTemplate);
    }

    @Transactional
    public ActivityTemplateEntity saveActivityTemplate(CreateActivityTemplateRQ createRQ) {
        ActivityTemplateEntity template = templateRepository.findByNameAndOwner_OwnerId(createRQ.getName(), createRQ.getOwnerId());
        if (template != null) {
            throw new DuplicateEntityException("Template with such name and owner already exists");
        }
        try {
            List<ActivityBlockConfigEntity> blocks = createRQ.getBlocks().stream()
                    .map(blockRQ -> {
                        ActivityBlockEntity block = activityBlockService.createActivityBlock(blockRQ.getBlock());
                        return ActivityBlockConfigEntity.builder()
                                .blockOrder(blockRQ.getOrder())
                                .isOptional(blockRQ.getIsOptional())
                                .blockId(block.getBlockId())
                                .blockType(block.getType())
                                .build();
                    }).toList();

            template = ActivityTemplateEntity.builder()
                    .templateId(UUID.randomUUID().toString())
                    .name(createRQ.getName())
                    .description(createRQ.getDescription())
                    .previewImageId(createRQ.getPreviewImageId())
                    .tags(createRQ.getTags())
                    .type(activityTypeService.getEntityByType(createRQ.getActivityType()))
                    .owner(ActivityOwnerEntity.builder()
                            .createdById(getUsername())
                            .ownerId(createRQ.getOwnerId())
                            .dateTimeCreated(LocalDateTime.now())
                            .scope(createRQ.getScope())
                            .build())
                    .blocks(blocks)
                    .build();

            template = templateRepository.save(template);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
        return template;
    }

    @Override
    public PageDto<ActivityTemplateDto> findActivityTemplates(SearchQuery searchQuery) {
        Specification<ActivityTemplateEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        Page<ActivityTemplateEntity> page = templateRepository.findAll(specification, pageable);
        return GeneralSpecification.getPageResponse(page, businessMapper::activityTemplateToDto);
    }

    @Override
    public PageDto<ActivityTemplateDto> findActivityTemplatesInfo(SearchQuery searchQuery) {
        Specification<ActivityTemplateEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        Page<ActivityTemplateEntity> page = templateRepository.findAll(specification, pageable);
        return GeneralSpecification.getPageResponse(page, businessMapper::activityTemplateToDtoInfo);
    }

    @Override
    public PageDto<ActivityTemplateDto> findMyActivityTemplates(SearchQuery searchQuery) {
        searchQuery.appendSearchFilter(new SearchFilter("owner.createdById", SearchOperators.EQUALS, CompareOption.AND, getUsername()));
        return findActivityTemplates(searchQuery);
    }

    @Override
    public List<ActivityTemplateGroupDto> findMyActivityTemplatesGroups(String ownerId) {
        List<ActivityTemplateGroupDto> groups = new ArrayList<>();

        groups.add(ActivityTemplateGroupDto.builder()
                .group("Public templates")
                .templates(templateRepository.findAllByOwner_Scope(ActivityScope.PUBLIC)
                        .stream().map(businessMapper::activityTemplateToDto)
                        .toList())
                .build());
        if (ownerId != null && !ownerId.isEmpty()) {
            groups.add(ActivityTemplateGroupDto.builder()
                    .group("Team templates")
                    .templates(templateRepository.findAllByOwner_ScopeAndOwner_OwnerId(ActivityScope.PRIVATE, ownerId)
                            .stream().map(businessMapper::activityTemplateToDto)
                            .toList())
                    .build());
        }

        return groups;
    }

    @Override
    public ActivityTemplateDto findActivityTemplateById(String templateId) {
        ActivityTemplateEntity template = templateRepository.findByTemplateId(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template with such id is not found"));
        return businessMapper.activityTemplateToDto(template);
    }

    @Override
    public ActivityTemplateDto findActivityTemplateByIdAndOwner(String templateId, String ownerId) {
        ActivityTemplateEntity template = templateRepository.findByTemplateId(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template with such id is not found"));
        if (template.getOwner().getOwnerId() == null || !template.getOwner().getOwnerId().equals(ownerId)) {
            throw new ServiceErrorException("You are not owner of this template");
        }
        return businessMapper.activityTemplateToDto(template);
    }

    @Override
    @Transactional
    public void deleteActivityTemplate(String templateId, String ownerId) {
        ActivityTemplateEntity template = templateRepository.findByTemplateIdAndOwner_OwnerId(templateId, ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Template with such id is not found"));
        if (template.getOwner().getOwnerId() == null || !template.getOwner().getOwnerId().equals(ownerId)) {
            throw new ServiceErrorException("You are not owner of this template");
        }
        for (ActivityBlockConfigEntity block : template.getBlocks()) {
            activityBlockService.deleteActivityBlock(block.getBlockId(), block.getBlockType());
        }
        templateRepository.delete(template);
    }

    @Override
    public ActivityTemplateEntity getActivityTemplate(String templateId) {
        return templateRepository.findByTemplateId(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template with such id is not found"));
    }
}
