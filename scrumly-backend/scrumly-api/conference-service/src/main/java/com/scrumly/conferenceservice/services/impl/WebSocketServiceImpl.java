package com.scrumly.conferenceservice.services.impl;

import com.scrumly.conferenceservice.dto.ConferenceConfigDto;
import com.scrumly.conferenceservice.dto.user.UserProfileDto;
import com.scrumly.conferenceservice.services.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    public void sendJoinConferenceMessage(String conferenceId, UserProfileDto profileDto) {
        try {
            Map<String, Object> joinMessage = new HashMap<>();
            joinMessage.put("conferenceId", conferenceId);
            joinMessage.put("profileDto", profileDto);
            messagingTemplate.convertAndSend("/topic/conference/" + conferenceId + "/join", joinMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendExitConferenceMessage(String conferenceId, UserProfileDto profileDto) {
        try {
            Map<String, Object> joinMessage = new HashMap<>();
            joinMessage.put("conferenceId", conferenceId);
            joinMessage.put("profileDto", profileDto);
            messagingTemplate.convertAndSend("/topic/conference/" + conferenceId + "/exit", joinMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendChangeRemoteConfig(String conferenceId, ConferenceConfigDto configDto) {
        try {
            Map<String, Object> configMessage = new HashMap<>();
            configMessage.put("conferenceId", conferenceId);
            configMessage.put("configDto", configDto);
            messagingTemplate.convertAndSend("/topic/conference/" + conferenceId + "/config", configMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
