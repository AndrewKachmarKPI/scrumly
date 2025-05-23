package com.scrumly.integrationservice.dto.googleCalendar;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventReminder;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateGoogleCalendarEventDto {
    private String summary;
    private String location;
    private String description;
    private String startDateTime;
    private String startTimeZone;
    private String endDateTime;
    private String endTimeZone;
    private List<String> recurrence;
    private List<AttendeeDto> attendees;
    private List<ReminderDto> reminders;
    private boolean createConference;


    @Getter
    @Setter
    @AllArgsConstructor
    public static class ReminderDto {
        private String method;
        private int minutes;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class AttendeeDto {
        private final String displayName;
        private final String emailAddress;
    }


    public List<EventAttendee> getEventAttendees() {
        if (attendees == null) {
            return new ArrayList<>();
        }
        return attendees.stream()
                .map(attendee -> new EventAttendee()
                        .setDisplayName(attendee.getDisplayName())
                        .setEmail(attendee.getEmailAddress()))
                .toList();
    }

    public Event.Reminders getReminders() {
        if (reminders == null) {
            return new Event.Reminders()
                    .setUseDefault(true);
        }
        List<EventReminder> eventReminder = reminders.stream()
                .map(reminder -> new EventReminder()
                        .setMethod(reminder.getMethod())
                        .setMinutes(reminder.getMinutes()))
                .collect(Collectors.toList());
        ;
        return new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(eventReminder);
    }

    public List<String> getRecurrence() {
        if (recurrence == null) {
            return new ArrayList<>();
        }
        return recurrence.stream()
                .map(rule -> "RRULE:" + rule)
                .collect(Collectors.toList());
    }
}
