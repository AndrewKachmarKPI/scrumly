package com.scrumly.eventservice.services;

import com.scrumly.eventservice.dto.activity.ActivityDto;
import com.scrumly.eventservice.dto.requests.events.CreateActivityRQ;
import com.scrumly.eventservice.dto.requests.events.ScheduleActivityCalendarEventRQ;
import com.scrumly.eventservice.dto.requests.events.StartActivityRQ;
import com.scrumly.eventservice.dto.statistic.ActivityUserStatistic;
import com.scrumly.eventservice.enums.ActivityStatus;
import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchQuery;

import java.util.List;

public interface ActivityService {
    ActivityDto startActivity(StartActivityRQ startActivityRQ);

    List<ActivityDto> scheduleActivity(CreateActivityRQ createActivityRQ);

    ActivityDto rescheduleActivity(String activityId, CreateActivityRQ createActivityRQ);

    ActivityDto changeActivityStatus(String activityId, ActivityStatus status);

    List<ActivityDto> rescheduleRecurrentActivity(String activityId, CreateActivityRQ createActivityRQ);

    ActivityDto scheduleActivityCalendarEvent(ScheduleActivityCalendarEventRQ rq);

    PageDto<ActivityDto> findActivities(SearchQuery searchQuery);

    ActivityDto getActivityById(String activityId);

    List<ActivityDto> getRecentTemplateActivities(String teamId, String templateId);

    ActivityUserStatistic getActivityStatistic(String username);

    void deleteActivity(String activityId);

    void deleteAllRecurrentActivity(String recurrentEventId);

    void cancelActivity(String activityId);

    void cancelAllRecurrentActivity(String recurrentEventId);

    void saveRoomReference(String activityId, String roomReference);
}
