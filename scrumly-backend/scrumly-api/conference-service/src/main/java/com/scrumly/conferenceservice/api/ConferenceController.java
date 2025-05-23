package com.scrumly.conferenceservice.api;

import com.scrumly.conferenceservice.dto.ConferenceConfigDto;
import com.scrumly.conferenceservice.dto.ConferenceRoomDto;
import com.scrumly.conferenceservice.dto.user.UserProfileDto;
import com.scrumly.conferenceservice.services.ConferenceService;
import com.scrumly.conferenceservice.utils.RetryException;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/conference")
public class ConferenceController {

    private final ConferenceService conferenceService;

    @PostMapping("/{workspaceId}/workspace")
    public ResponseEntity<String> createWorkspaceConference(@PathVariable("workspaceId") @NotNull @NotEmpty String workspaceId) {
        return ResponseEntity.ok(conferenceService.createConference(workspaceId));
    }

    @DeleteMapping("/{conferenceId}")
    public ResponseEntity<Void> deleteConference(@PathVariable("conferenceId") @NotNull @NotEmpty String conferenceId) {
        conferenceService.deleteConference(conferenceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{conferenceId}")
    public ResponseEntity<ConferenceRoomDto> joinConference(@PathVariable("conferenceId") @NotNull @NotEmpty String conferenceId) {
        try {
            return ResponseEntity.ok(conferenceService.joinConference(conferenceId));
        } catch (OpenViduJavaClientException | OpenViduHttpException | RetryException | InterruptedException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{conferenceId}/isJoined")
    public ResponseEntity<Boolean> isJoined(@PathVariable("conferenceId") @NotNull @NotEmpty String conferenceId) {
        return ResponseEntity.ok(conferenceService.isJoined(conferenceId));
    }

    @PostMapping("/{conferenceId}/exit")
    public ResponseEntity<Void> exitConference(@PathVariable("conferenceId") @NotNull @NotEmpty String conferenceId) {
        try {
            conferenceService.exitConference(conferenceId);
            return ResponseEntity.ok().build();
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{conferenceId}/kick/{userId}")
    public ResponseEntity<Void> kickFromConference(
            @PathVariable("conferenceId") @NotNull @NotEmpty String conferenceId,
            @PathVariable("userId") @NotNull @NotEmpty String userId) {
        try {
            conferenceService.kickFromConference(conferenceId, userId);
            return ResponseEntity.ok().build();
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{conferenceId}/close")
    public ResponseEntity<Void> closeConference(@PathVariable("conferenceId") @NotNull @NotEmpty String conferenceId) {
        try {
            conferenceService.closeConference(conferenceId);
            return ResponseEntity.ok().build();
        } catch (OpenViduJavaClientException | OpenViduHttpException | RetryException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{conferenceId}/sessions")
    public ResponseEntity<List<UserProfileDto>> getActiveSessions(@PathVariable("conferenceId") @NotNull @NotEmpty String conferenceId) {
        return ResponseEntity.ok(conferenceService.getActiveSessions(conferenceId));
    }

    @PutMapping("/{conferenceId}/remote/config")
    public ResponseEntity<Void> changeRemoteConferenceConfig(@PathVariable("conferenceId") @NotNull @NotEmpty String conferenceId,
                                                             @RequestBody ConferenceConfigDto configDto) {
        conferenceService.changeRemoteConferenceConfig(conferenceId, configDto);
        return ResponseEntity.accepted().build();
    }
}
