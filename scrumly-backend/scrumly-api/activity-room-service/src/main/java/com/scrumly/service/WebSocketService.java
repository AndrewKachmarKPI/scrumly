package com.scrumly.service;


import com.scrumly.domain.ActivityRoom;
import com.scrumly.domain.ActivityTimerState;
import com.scrumly.dto.events.OnActivityRoomClosed;
import com.scrumly.dto.user.UserConnectionStatus;

import java.util.List;

public interface WebSocketService {
    void sendActivityRoomCreated(ActivityRoom room);
    void sendActivityRoomClosed(OnActivityRoomClosed activityRoomClosed);
    void sendActivityRoomToUsers(ActivityRoom room, List<String> userId);
    void sendActivityRoomToUser(ActivityRoom room, String userId);
    void sendActivityTimerChange(ActivityRoom room, String userId, ActivityTimerState timerState);
    void sendUserStatusChange(ActivityRoom room, String userId, UserConnectionStatus userConnectionStatus);

}
