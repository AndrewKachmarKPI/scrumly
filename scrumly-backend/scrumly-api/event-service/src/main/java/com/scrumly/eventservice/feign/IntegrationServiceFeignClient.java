package com.scrumly.eventservice.feign;

import com.scrumly.dto.googleCalendar.CreateGoogleCalendarEventDto;
import com.scrumly.dto.googleCalendar.GoogleCalendarEventDto;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.eventservice.config.FeignClientConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "integration-service", configuration = FeignClientConfig.class)
public interface IntegrationServiceFeignClient {
    @GetMapping("/services/is-connected/user")
    ResponseEntity<Boolean> isConnected(@RequestParam("serviceType") ServiceType serviceType);
    @PostMapping("/google/calendar/events")
    ResponseEntity<GoogleCalendarEventDto> createGoogleCalendarEvent(@RequestBody CreateGoogleCalendarEventDto dto);
    @PutMapping("/google/calendar/events")
    ResponseEntity<GoogleCalendarEventDto> updateCalendarEvent(@RequestParam("eventId") @NotNull String eventId,
                                                               @Valid @RequestBody CreateGoogleCalendarEventDto dto);
    @DeleteMapping("/google/calendar/events")
    ResponseEntity<Void> deleteGoogleCalendarEvent(@RequestParam("eventId") @NotNull String eventId);

    @DeleteMapping("/google/calendar/events/recurrent")
    ResponseEntity<Void> deleteGoogleCalendarRecurrentEvent(@RequestParam(value = "eventId", required = false) String eventId,
                                                            @RequestParam("recurrenceEventId") @NotNull String recurrenceEventId);

}
