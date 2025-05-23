package com.scrumly.eventservice.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    public static Duration getDuration(String startDateTime, String endDateTime) {
        Instant startInstant = Instant.parse(startDateTime);
        Instant endInstant = Instant.parse(endDateTime);
        return Duration.between(startInstant, endInstant);
    }

    public static String getRecurrentEventId(String eventId, String startDateTime) {
        ZonedDateTime originalStart = ZonedDateTime
                .parse(startDateTime, DateTimeFormatter.ISO_DATE_TIME)
                .withZoneSameLocal(ZoneId.of("UTC"));
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String formattedTimestamp = outputFormatter.format(originalStart);
        return eventId.concat("_").concat(formattedTimestamp);
    }
}
