package com.scrumly.service.impl;

import com.scrumly.domain.ActivityRoom;
import com.scrumly.domain.ActivityTimerState;
import com.scrumly.dto.events.OnActivityRoomClosed;
import com.scrumly.dto.user.UserConnectionStatus;
import com.scrumly.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.scrumly.mappers.BusinessMapper.serializeMeetingRoom;

@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private static final String BASE_PATH = "/topic/activity/";
    private static final String BASE_USER_PATH = "/activity/";

    @Override
    public void sendActivityRoomCreated(ActivityRoom room) {
        String serializedRoom = serializeMeetingRoom(room);
        messagingTemplate.convertAndSend(BASE_PATH + room.getActivityId() + "/room/created", serializedRoom);
    }

    @Override
    public void sendActivityRoomClosed(OnActivityRoomClosed activityRoomClosed) {
        messagingTemplate.convertAndSend(BASE_PATH + activityRoomClosed.getActivityId() + "/room/closed", activityRoomClosed);
    }

    @Override
    public void sendActivityRoomToUsers(ActivityRoom room, List<String> userId) {
        for (String id : userId) {
            sendActivityRoomToUser(room, id);
        }
    }

    @Override
    public void sendActivityRoomToUser(ActivityRoom room, String userId) {
        String serializedRoom = serializeMeetingRoom(room);
        messagingTemplate.convertAndSendToUser(userId, BASE_USER_PATH + room.getActivityId() + "/room/change", serializedRoom);
    }

    @Override
    public void sendActivityTimerChange(ActivityRoom room, String userId, ActivityTimerState timerState) {
        messagingTemplate.convertAndSendToUser(userId, BASE_USER_PATH + room.getActivityId() + "/room/timer", timerState);
    }

    @Override
    public void sendUserStatusChange(ActivityRoom room, String userId, UserConnectionStatus userConnectionStatus) {
        messagingTemplate.convertAndSendToUser(userId, BASE_USER_PATH + room.getActivityId() + "/room/user", userConnectionStatus);
    }
}
