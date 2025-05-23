package com.scrumly.eventservice.feign;

import com.scrumly.dto.googleCalendar.CreateGoogleCalendarEventDto;
import com.scrumly.dto.googleCalendar.GoogleCalendarEventDto;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.eventservice.config.FeignClientConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "conference-service", configuration = FeignClientConfig.class)
public interface ConferenceServiceFeignClient {
    @PostMapping("/api/conference/{workspaceId}/workspace")
    String createWorkspaceConference(@PathVariable("workspaceId") @NotNull @NotEmpty String workspaceId);
    @DeleteMapping("/api/conference/{conferenceId}")
    void deleteConference(@PathVariable("conferenceId") @NotNull @NotEmpty String conferenceId);
}
