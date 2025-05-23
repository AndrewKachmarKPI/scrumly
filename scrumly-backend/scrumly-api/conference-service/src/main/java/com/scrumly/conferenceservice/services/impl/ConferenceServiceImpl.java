package com.scrumly.conferenceservice.services.impl;

import com.scrumly.conferenceservice.domain.ConferenceEntity;
import com.scrumly.conferenceservice.dto.ConferenceConfigDto;
import com.scrumly.conferenceservice.dto.ConferenceRoomDto;
import com.scrumly.conferenceservice.dto.user.UserProfileDto;
import com.scrumly.conferenceservice.enums.ConferenceStatus;
import com.scrumly.conferenceservice.enums.ConnectionType;
import com.scrumly.conferenceservice.feign.UserServiceFeignClient;
import com.scrumly.conferenceservice.repository.ConferenceEntityRepository;
import com.scrumly.conferenceservice.services.ConferenceService;
import com.scrumly.conferenceservice.services.OpenViduService;
import com.scrumly.conferenceservice.services.WebSocketService;
import com.scrumly.conferenceservice.utils.CodeGenerator;
import com.scrumly.conferenceservice.utils.RetryException;
import io.openvidu.java.client.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.scrumly.conferenceservice.utils.CodeGenerator.generateConferenceId;
import static com.scrumly.conferenceservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class ConferenceServiceImpl implements ConferenceService {
    private final ConferenceEntityRepository conferenceRepository;
    private final OpenViduService openViduService;
    private final UserServiceFeignClient userServiceFeignClient;
    private final WebSocketService webSocketService;


    @Value("${open-vidu.call.recording}")
    private String CALL_RECORDING;

    @Value("${open-vidu.call.broadcast}")
    private String CALL_BROADCAST;


    @Override
    public String createConference(String workspaceId) {
        ConferenceEntity conference = conferenceRepository.findByWorkspaceId(workspaceId);
        if (conference != null) {
            throw new EntityExistsException("Conference for this workspace already exists");
        }

        conference = ConferenceEntity.builder()
                .conferenceId(generateConferenceId())
                .workspaceId(workspaceId)
                .creatorId(getUsername())
                .createdDate(LocalDateTime.now())
                .createdDate(LocalDateTime.now())
                .status(ConferenceStatus.NEW)
                .build();
        conference = conferenceRepository.save(conference);
        return conference.getConferenceId();
    }

    @Override
    @Transactional
    public void deleteConference(String conferenceId) {
        ConferenceEntity conference = conferenceRepository.findByConferenceId(conferenceId);
        if (conference != null) {
            conferenceRepository.delete(conference);
        }
    }

    @Override
    public ConferenceRoomDto joinConference(String conferenceId) throws OpenViduJavaClientException, OpenViduHttpException, RetryException, InterruptedException {
        ConferenceEntity conference = conferenceRepository.findByConferenceId(conferenceId);
        if (conference == null) {
            throw new EntityNotFoundException("Conference is not found");
        }

        UserProfileDto profileDto = userServiceFeignClient.findMyUserProfile().getBody();
        OpenViduRole role = conference.getCreatorId().equals(profileDto.getUserId())
                ? OpenViduRole.MODERATOR
                : OpenViduRole.PUBLISHER;

        Session session = openViduService.createSession(conference.getConferenceId());
        Connection cameraConnection = openViduService.createConnection(session, ConnectionType.CAMERA, role, profileDto);
        Connection screenConnection = openViduService.createConnection(session, ConnectionType.SCREEN, role, profileDto);

        webSocketService.sendJoinConferenceMessage(conferenceId, profileDto);

        return ConferenceRoomDto.builder()
                .conferenceId(conferenceId)
                .workspaceId(conference.getWorkspaceId())
                .recordingEnabled(CALL_RECORDING.toUpperCase().equals("ENABLED"))
                .broadcastingEnabled(CALL_BROADCAST.toUpperCase().equals("ENABLED"))
                .isRecordingActive(session.isBeingRecorded())
                .isBroadcastingActive(session.isBeingBroadcasted())
                .recordings(new ArrayList<>())
                .cameraToken(cameraConnection.getToken())
                .screenToken(screenConnection.getToken())
                .build();
    }

    @Override
    public void exitConference(String conferenceId) throws OpenViduJavaClientException, OpenViduHttpException {
        ConferenceEntity conference = conferenceRepository.findByConferenceId(conferenceId);
        if (conference == null) {
            throw new EntityNotFoundException("Conference is not found");
        }
        openViduService.closeConnections(conferenceId, getUsername());
        UserProfileDto profileDto = userServiceFeignClient.findMyUserProfile().getBody();
        webSocketService.sendExitConferenceMessage(conferenceId, profileDto);
    }

    @Override
    public void kickFromConference(String conferenceId, String userId) throws OpenViduJavaClientException, OpenViduHttpException {
        ConferenceEntity conference = conferenceRepository.findByConferenceId(conferenceId);
        if (conference == null) {
            throw new EntityNotFoundException("Conference is not found");
        }
        if (!conference.getCreatorId().equals(getUsername())) {
            throw new RuntimeException("You are not allowed to kick users");
        }
        openViduService.closeConnections(conferenceId, userId);
    }

    @Override
    public void closeConference(String conferenceId) throws OpenViduJavaClientException, OpenViduHttpException, RetryException, InterruptedException {
        ConferenceEntity conference = conferenceRepository.findByConferenceId(conferenceId);
        if (conference == null) {
            throw new EntityNotFoundException("Conference is not found");
        }
        openViduService.closeSession(conferenceId);
        UserProfileDto profileDto = userServiceFeignClient.findMyUserProfile().getBody();
        webSocketService.sendExitConferenceMessage(conferenceId, profileDto);
    }

    @Override
    public boolean isJoined(String conferenceId) {
        ConferenceEntity conference = conferenceRepository.findByConferenceId(conferenceId);
        if (conference == null) {
            throw new EntityNotFoundException("Conference is not found");
        }
        return openViduService.hasConnection(conferenceId, getUsername());
    }

    @Override
    public List<UserProfileDto> getActiveSessions(String conferenceId) {
        ConferenceEntity conference = conferenceRepository.findByConferenceId(conferenceId);
        if (conference == null) {
            throw new EntityNotFoundException("Conference is not found");
        }
        Set<String> connections = openViduService.getActiveConnectionsUserIds(conferenceId);
        List<UserProfileDto> users = userServiceFeignClient.findUsers(connections).getBody();
        return users;
    }

    @Override
    public void changeRemoteConferenceConfig(String conferenceId, ConferenceConfigDto configDto) {
        ConferenceEntity conference = conferenceRepository.findByConferenceId(conferenceId);
        if (conference == null) {
            throw new EntityNotFoundException("Conference is not found");
        }
        webSocketService.sendChangeRemoteConfig(conferenceId, configDto);
    }
}
