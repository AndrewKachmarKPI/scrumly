package com.scrumly.service;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.domain.ActivityRoomReport;

public interface ActivityRoomReportService {
    ActivityRoomReport createActivityRoomReport(ActivityRoom room);
    ActivityRoomReport getActivityRoomReport(String activityId);

    ActivityRoomReport test(ActivityRoom room);
}
