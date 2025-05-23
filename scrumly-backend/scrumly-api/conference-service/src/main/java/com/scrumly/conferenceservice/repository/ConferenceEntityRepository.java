package com.scrumly.conferenceservice.repository;

import com.scrumly.conferenceservice.domain.ConferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConferenceEntityRepository extends JpaRepository<ConferenceEntity, Long> {
    ConferenceEntity findByWorkspaceId(String workspaceId);
    ConferenceEntity findByConferenceId(String conferenceId);

}
