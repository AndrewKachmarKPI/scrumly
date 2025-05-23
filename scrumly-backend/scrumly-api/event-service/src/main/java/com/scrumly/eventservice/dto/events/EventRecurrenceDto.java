package com.scrumly.eventservice.dto.events;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class EventRecurrenceDto {
    private Long id;
    private String recurringEventId;
    private String recurrence;
    private String recurrenceText;

    public EventRecurrenceDto(EventRecurrenceDto other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null EventRecurrenceDto");
        }
        this.id = other.id;
        this.recurringEventId = other.recurringEventId;
        this.recurrence = other.recurrence;
        this.recurrenceText = other.recurrenceText;
    }
}
