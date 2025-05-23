package com.scrumly.eventservice.repository;

import com.scrumly.eventservice.domain.events.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {
    EventEntity findByEventIdAndCreatedBy(String eventId, String createdBy);

    List<EventEntity> findAllByCreatedFor(String createdFor);

    List<EventEntity> findAllByCreatedByAndRecurrence_RecurringEventId(String createdBy, String recurrentEventId);

    List<EventEntity> findAllByEventIdInAndCreatedBy(List<String> eventId, String createdBy);
}
