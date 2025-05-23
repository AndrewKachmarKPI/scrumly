package com.scrumly.userservice.userservice.repository;

import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileEntityRepository extends JpaRepository<UserProfileEntity, Long>, JpaSpecificationExecutor<UserProfileEntity> {
    boolean existsByEmail(String email);

    boolean existsByUserId(String userId);

    UserProfileEntity findByUserId(String userId);
    UserProfileEntity findByEmail(String email);

    List<UserProfileEntity> findAllByUserIdIn(List<String> usernames);
}
