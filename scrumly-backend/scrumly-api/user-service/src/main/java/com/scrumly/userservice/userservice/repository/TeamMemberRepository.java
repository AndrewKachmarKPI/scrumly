package com.scrumly.userservice.userservice.repository;

import com.scrumly.userservice.userservice.domain.user.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository extends JpaRepository<UserProfileEntity, Long> {
}
