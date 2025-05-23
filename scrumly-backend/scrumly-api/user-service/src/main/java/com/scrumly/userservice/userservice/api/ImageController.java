package com.scrumly.userservice.userservice.api;

import com.scrumly.dto.ImageRequestDto;
import com.scrumly.userservice.userservice.domain.ImageEntity;
import com.scrumly.userservice.userservice.dto.service.ImageDto;
import com.scrumly.userservice.userservice.services.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
@Validated
public class ImageController {
    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<String> saveImage(@RequestBody MultipartFile file) {
        ImageEntity imageEntity = imageService.saveImage(file);
        return ResponseEntity.ok(imageEntity.getImageId());
    }

    @PostMapping("/parsed")
    public ResponseEntity<String> saveImageParsed(@RequestBody ImageRequestDto imageRequestDto) {
        ImageEntity imageEntity = imageService.saveImage(imageRequestDto);
        return ResponseEntity.ok(imageEntity.getImageId());
    }

    @PutMapping("/{imageId}")
    public ResponseEntity<String> copyImage(@PathVariable("imageId") String imageId) {
        return ResponseEntity.ok(imageService.copyImage(imageId));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImageById(@PathVariable("imageId") String imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> findImageById(@PathVariable("imageId") String imageId) {
        ImageDto imageDto = imageService.findImageById(imageId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, imageDto.getType())
                .body(imageDto.getData());
    }
}
