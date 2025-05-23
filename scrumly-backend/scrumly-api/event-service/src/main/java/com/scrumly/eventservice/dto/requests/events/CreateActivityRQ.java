package com.scrumly.eventservice.dto.requests.events;

import com.scrumly.enums.integration.ServiceType;
import com.scrumly.eventservice.dto.events.EventDto;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CreateActivityRQ {
    @NotNull
    private EventDto eventDto;
    private String conferenceProvider;
    private String calendarProvider;
    private boolean createCalendarEvent;
    private boolean createConference;
    private String templateId;

    public CreateActivityRQ(CreateActivityRQ rq) {
        this.eventDto = new EventDto(rq.getEventDto());
        this.conferenceProvider = rq.getConferenceProvider();
        this.calendarProvider = rq.getCalendarProvider();
        this.createCalendarEvent = rq.createCalendarEvent;
        this.createConference = rq.createConference;
        this.templateId = rq.getTemplateId();
    }
}
