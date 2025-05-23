package com.scrumly.userservice.userservice.repository;

import com.scrumly.userservice.userservice.domain.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageEntityRepository extends JpaRepository<ImageEntity, Long> {
    ImageEntity findByImageId(String imageId);
}
