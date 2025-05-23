package com.scrumly.repository;

import com.scrumly.domain.ActivityRoomReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRoomReportRepository extends JpaRepository<ActivityRoomReportEntity, Long> {
    ActivityRoomReportEntity findByActivityId(String activityId);
    void deleteByActivityId(String activityId);
}
