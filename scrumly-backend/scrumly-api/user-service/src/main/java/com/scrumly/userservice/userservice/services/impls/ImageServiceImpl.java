package com.scrumly.userservice.userservice.services.impls;

import com.scrumly.dto.ImageRequestDto;
import com.scrumly.exceptions.enums.ServiceErrorCode;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.userservice.userservice.domain.ImageEntity;
import com.scrumly.userservice.userservice.dto.service.ImageDto;
import com.scrumly.userservice.userservice.repository.ImageEntityRepository;
import com.scrumly.userservice.userservice.services.ImageService;
import com.scrumly.userservice.userservice.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final ImageEntityRepository imageRepository;
    private final ModelMapper modelMapper;

    @Override
    public ImageEntity saveImage(ImageRequestDto imageRequestDto) {
        if (imageRequestDto == null) {
            return null;
        }
        ImageEntity imageEntity = imageRepository.save(ImageEntity.builder()
                .imageId(UUID.randomUUID().toString())
                .name(imageRequestDto.getFileName())
                .type(imageRequestDto.getType())
                .data(ImageUtils.compressImage(imageRequestDto.getImage()))
                .build());
        return imageEntity;
    }

    @Override
    public ImageEntity saveImage(MultipartFile image) {
        if (image == null) {
            return null;
        }
        ImageEntity imageEntity = new ImageEntity();
        try {
            imageEntity = imageRepository.save(ImageEntity.builder()
                    .imageId(UUID.randomUUID().toString())
                    .name(image.getOriginalFilename())
                    .type(image.getContentType())
                    .data(ImageUtils.compressImage(image.getBytes()))
                    .build());
        } catch (IOException e) {
            throw new ServiceErrorException(e);
        }
        return imageEntity;
    }

    @Override
    public String copyImage(String imageId) {
        ImageEntity imageEntity = imageRepository.findByImageId(imageId);
        if (imageEntity == null) {
            throw new EntityNotFoundException(ServiceErrorCode.IMAGE_NOTFOUND);
        }
        ImageEntity newImage = new ImageEntity(imageEntity);
        newImage.setImageId(UUID.randomUUID().toString());
        newImage = imageRepository.save(newImage);
        return newImage.getImageId();
    }

    @Override
    public ImageDto findImageById(String imageId) {
        ImageEntity imageEntity = imageRepository.findByImageId(imageId);
        if (imageEntity == null) {
            throw new EntityNotFoundException(ServiceErrorCode.IMAGE_NOTFOUND);
        }
        return ImageDto.builder()
                .id(imageEntity.getId())
                .imageId(imageEntity.getImageId())
                .type(imageEntity.getType())
                .name(imageEntity.getName())
                .data(ImageUtils.decompressImage(imageEntity.getData()))
                .build();
    }

    @Override
    public void deleteImage(String imageId) {
        ImageEntity imageEntity = imageRepository.findByImageId(imageId);
        if (imageEntity != null) {
            imageRepository.delete(imageEntity);
        }
    }
}
