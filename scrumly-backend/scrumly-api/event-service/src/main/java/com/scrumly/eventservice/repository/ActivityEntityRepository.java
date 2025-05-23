package com.scrumly.eventservice.repository;

import com.scrumly.eventservice.domain.activity.ActivityEntity;
import com.scrumly.eventservice.enums.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityEntityRepository extends JpaRepository<ActivityEntity, Long>, JpaSpecificationExecutor<ActivityEntity> {
    Optional<ActivityEntity> findByActivityId(String activityId);
    ActivityEntity getByActivityId(String activityId);
    Boolean existsByActivityId(String activityId);

    List<ActivityEntity> findAllByRecurringEventId(String recurringEventId);
    List<ActivityEntity> findAllByRecurringEventIdAndStatus(String recurringEventId, ActivityStatus status);
}
