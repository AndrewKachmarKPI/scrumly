package com.scrumly.repository;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.domain.ActivityRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRoomRepository extends JpaRepository<ActivityRoomEntity, Long> {
    ActivityRoomEntity findActivityRoomByActivityId(String activityId);
    Boolean existsByActivityId(String activityId);
}
