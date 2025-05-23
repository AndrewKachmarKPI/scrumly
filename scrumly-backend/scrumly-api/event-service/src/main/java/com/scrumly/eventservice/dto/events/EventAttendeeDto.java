package com.scrumly.eventservice.dto.events;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class EventAttendeeDto {
    private Long id;
    private String userId;
    private String userEmailAddress;
    private String displayName;
}
