package com.scrumly.userservice.userservice.services;

import com.scrumly.dto.ImageRequestDto;
import com.scrumly.userservice.userservice.domain.ImageEntity;
import com.scrumly.userservice.userservice.dto.service.ImageDto;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    ImageEntity saveImage(ImageRequestDto imageRequestDto);
    ImageEntity saveImage(MultipartFile image);

    String copyImage(String imageId);

    ImageDto findImageById(String imageId);

    void deleteImage(String imageId);
}
