package com.scrumly.dto.events;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.domain.ActivityRoomReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
public class OnActivityRoomClosed {
    private String activityId;
    private String activityRoom;
    private String activityRoomReport;
}
