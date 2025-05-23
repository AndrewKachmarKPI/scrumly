package com.scrumly.eventservice.domain.events;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@Entity
public class EventRecurrenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private String recurringEventId;
    @Column
    private String recurrence;
    @Column
    private String recurrenceText;

    public EventRecurrenceEntity(EventRecurrenceEntity recurrence) {
        this.recurringEventId = recurrence.getRecurringEventId();
        this.recurrence = recurrence.getRecurrence();
        this.recurrenceText = recurrence.getRecurrenceText();
    }
}
