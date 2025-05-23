package com.scrumly.conferenceservice.services;

import com.scrumly.conferenceservice.dto.ConferenceConfigDto;
import com.scrumly.conferenceservice.dto.ConferenceRoomDto;
import com.scrumly.conferenceservice.dto.user.UserProfileDto;
import com.scrumly.conferenceservice.utils.RetryException;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;

import java.util.List;

public interface ConferenceService {
    String createConference(String workspaceId);

    void deleteConference(String workspaceId);

    ConferenceRoomDto joinConference(String conferenceId) throws OpenViduJavaClientException, OpenViduHttpException, RetryException, InterruptedException;

    void exitConference(String conferenceId) throws OpenViduJavaClientException, OpenViduHttpException;

    void kickFromConference(String conferenceId, String userId) throws OpenViduJavaClientException, OpenViduHttpException;

    void closeConference(String conferenceId) throws OpenViduJavaClientException, OpenViduHttpException, RetryException, InterruptedException;

    boolean isJoined(String conferenceId);

    List<UserProfileDto> getActiveSessions(String conferenceId);

    void changeRemoteConferenceConfig(String conferenceId, ConferenceConfigDto configDto);
}
