package com.scrumly.conferenceservice.services;

import com.scrumly.conferenceservice.dto.ConferenceConfigDto;
import com.scrumly.conferenceservice.dto.user.UserProfileDto;

public interface WebSocketService {
    void sendJoinConferenceMessage(String conferenceId, UserProfileDto profileDto);
    void sendExitConferenceMessage(String conferenceId, UserProfileDto profileDto);
    void sendChangeRemoteConfig(String conferenceId, ConferenceConfigDto configDto);
}
