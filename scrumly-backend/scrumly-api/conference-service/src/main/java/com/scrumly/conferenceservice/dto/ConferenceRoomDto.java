package com.scrumly.conferenceservice.dto;

import io.openvidu.java.client.Recording;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@EqualsAndHashCode
public class ConferenceRoomDto {
    private String conferenceId;
    private String workspaceId;
    private boolean recordingEnabled;
    private boolean broadcastingEnabled;
    private boolean isRecordingActive;
    private boolean isBroadcastingActive;
    private List<Recording> recordings;
    private String cameraToken;
    private String screenToken;
}
