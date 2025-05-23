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
public class EventAttendeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private String userEmailAddress;
    @Column(nullable = false)
    private String displayName;

    public EventAttendeeEntity(EventAttendeeEntity other) {
        this.userId = other.userId;
        this.userEmailAddress = other.userEmailAddress;
        this.displayName = other.displayName;
    }
}
