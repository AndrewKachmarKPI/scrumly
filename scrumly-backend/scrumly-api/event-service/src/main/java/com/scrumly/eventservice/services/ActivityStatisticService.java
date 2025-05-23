package com.scrumly.eventservice.services;

import com.scrumly.eventservice.dto.statistic.ActivityUserStatistic;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ActivityStatisticService {
    ActivityUserStatistic getActivityUserStatistic(String username, LocalDate startDate, LocalDate end);
}
