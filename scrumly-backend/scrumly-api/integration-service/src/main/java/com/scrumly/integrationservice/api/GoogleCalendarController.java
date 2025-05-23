package com.scrumly.integrationservice.api;

import com.google.api.services.calendar.model.Events;
import com.scrumly.enums.integration.ServiceType;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.integrationservice.dto.ServiceAuthorizeRQ;
import com.scrumly.integrationservice.dto.googleCalendar.CreateGoogleCalendarEventDto;
import com.scrumly.integrationservice.dto.googleCalendar.GetGoogleCalendarEventsDto;
import com.scrumly.integrationservice.dto.googleCalendar.GoogleCalendarEventDto;
import com.scrumly.integrationservice.service.google.GoogleCalendarService;
import com.scrumly.integrationservice.service.IntegrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.scrumly.integrationservice.utils.SecurityUtils.getUsername;

@CrossOrigin
@RestController
@RequestMapping("/google/calendar")
@RequiredArgsConstructor
@Validated
public class GoogleCalendarController {
    private final GoogleCalendarService googleCalendarService;
    private final IntegrationService integrationService;


    @GetMapping("/authorize")
    public String getAuthorizationUrl() {
        return googleCalendarService.getAuthorizationUrl();
    }

    @PostMapping("/authorize")
    public ResponseEntity<Void> authorize(@RequestParam(value = "code") String code) {
        integrationService.authorize(ServiceAuthorizeRQ.builder()
                .code(code)
                .connectingId(getUsername())
                .serviceType(ServiceType.GOOGLE_CALENDAR)
                .build());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/search")
    public ResponseEntity<Events> getCalendarEvents(@Valid @RequestBody GetGoogleCalendarEventsDto calendarEventsDto) {
        return ResponseEntity.ok(googleCalendarService.getCalendarEventList(calendarEventsDto));
    }

    @PostMapping("/events")
    public ResponseEntity<GoogleCalendarEventDto> createCalendarEvent(@Valid @RequestBody CreateGoogleCalendarEventDto dto) {
        return ResponseEntity.ok(googleCalendarService.createCalendarEvent(dto));
    }

    @PutMapping("/events")
    public ResponseEntity<GoogleCalendarEventDto> updateCalendarEvent(@RequestParam("eventId") @NotNull String eventId,
                                                                      @Valid @RequestBody CreateGoogleCalendarEventDto dto) {
        return ResponseEntity.ok(googleCalendarService.updateCalendarEvent(eventId, dto));
    }

    @DeleteMapping("/events")
    public ResponseEntity<Void> deleteCalendarEvent(@RequestParam("eventId") @NotNull String eventId) {
        googleCalendarService.deleteCalendarEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/events/recurrent")
    public ResponseEntity<Void> deleteGoogleCalendarRecurrentEvent(@RequestParam(value = "eventId", required = false) String eventId,
                                                                   @RequestParam("recurrenceEventId") @NotNull String recurrenceEventId) {
        googleCalendarService.deleteCalendarRecurrentEvent(eventId, recurrenceEventId);
        return ResponseEntity.noContent().build();
    }
}
