package com.scrumly.eventservice.utils;


import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.RRule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


public class RecurrenceRuleParser {
    public static List<LocalDateTime> parseRecurrence(String rule, String startDateTime) throws ParseException {
        RRule rrule = new RRule(rule);
        Recur recur = rrule.getRecur();

        // Convert startDateTime to net.fortuna.ical4j.model.DateTime
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        java.util.Date parsedStartDate = sdf.parse(startDateTime);
        DateTime start = new DateTime(parsedStartDate);

        // Determine endDateTime
        DateTime end;
        if (recur.getUntil() != null) {
            end = new DateTime(recur.getUntil()); // Use recurrence end date if set
        } else {
            // Default to one month later
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(parsedStartDate);
            cal.add(java.util.Calendar.MONTH, 1);
            end = new DateTime(cal.getTime());
        }

        // Generate recurrence dates
        DateList dates = recur.getDates(start, end, Value.DATE_TIME);
        List<LocalDateTime> localDateTimes = new ArrayList<>();

        dates.forEach(date -> {
            LocalDateTime localDateTime = ((DateTime) date).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            localDateTimes.add(localDateTime);
        });

        return localDateTimes;
    }

    private static java.util.Date addOneMonth(java.util.Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusMonths(1);
        return java.util.Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }


}
