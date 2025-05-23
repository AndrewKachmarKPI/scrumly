package com.scrumly.service;


import com.scrumly.domain.ActivityRoom;
import com.scrumly.domain.ActivityTimerState;
import com.scrumly.dto.events.SyncBlockOption;
import com.scrumly.dto.user.UserConnectionStatus;

import java.util.List;

public interface ActivityRoomService {
    ActivityRoom createRoom(String activityId);

    void onFinishActivity(String activityId);

    ActivityRoom getActivityRoom(String activityId);

    ActivityRoom getActivityRoomOrThrow(String activityId);

    Boolean isActivityRoomCreated(String activityId);

    void joinRoom(String activityId);

    void exitRoom(String activityId);

    void test(String activityId);

    List<SyncBlockOption> getSyncBlockOptions(String activityId);

    //    NOTIFY EVENTS
    void save(ActivityRoom activityRoom);

    void saveAndNotifyAll(ActivityRoom activityRoom);

    void saveAndNotifyUser(ActivityRoom activityRoom, String username);

    void notify(ActivityRoom activityRoom, String userId);

    void notifyAll(ActivityRoom activityRoom);

    void notifyTimerChange(ActivityRoom activityRoom, ActivityTimerState timerState);

    void sendUserStatusChange(ActivityRoom activityRoom, String userId, UserConnectionStatus connectionStatus);
}
