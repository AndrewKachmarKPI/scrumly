package com.scrumly.eventservice.api;

import com.scrumly.eventservice.dto.events.EventDto;
import com.scrumly.eventservice.dto.requests.events.CreateActivityRQ;
import com.scrumly.eventservice.services.EventService;
import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@CrossOrigin
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;


    @GetMapping
    public ResponseEntity<List<EventDto>> getAllEvents() {
        List<EventDto> events = eventService.getAllEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/createdFor/{createdFor}")
    public ResponseEntity<List<EventDto>> getAllEventsByCreatedFor(@PathVariable String createdFor) {
        List<EventDto> events = eventService.getAllEvents(createdFor);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PostMapping("/search")
    public ResponseEntity<PageDto<EventDto>> findEvents(@RequestBody SearchQuery searchQuery) {
        PageDto<EventDto> eventsPage = eventService.findEvents(searchQuery);
        return new ResponseEntity<>(eventsPage, HttpStatus.OK);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEventById(@PathVariable String eventId) {
        EventDto event = eventService.getEventById(eventId);
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<List<EventDto>> createEvent(@RequestBody CreateActivityRQ createActivityRQ) {
        return new ResponseEntity<>(eventService.createEvent(createActivityRQ), HttpStatus.CREATED);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable String eventId, @RequestBody EventDto eventDTO) {
        EventDto updatedEvent = eventService.updateEvent(eventId, eventDTO);
        return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String eventId) {
        eventService.cancelEvent(eventId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

