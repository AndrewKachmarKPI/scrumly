package com.scrumly.eventservice.services.impl;

import com.scrumly.enums.userservice.PermissionType;
import com.scrumly.eventservice.domain.ActivityTemplateEntity;
import com.scrumly.eventservice.domain.activity.ActivityEntity;
import com.scrumly.eventservice.domain.activity.ActivityHistory;
import com.scrumly.eventservice.domain.events.EventEntity;
import com.scrumly.eventservice.domain.workspace.WorkspaceEntity;
import com.scrumly.eventservice.dto.activity.ActivityDto;
import com.scrumly.eventservice.dto.events.EventDto;
import com.scrumly.eventservice.dto.requests.events.CreateActivityRQ;
import com.scrumly.eventservice.dto.requests.events.ScheduleActivityCalendarEventRQ;
import com.scrumly.eventservice.dto.requests.events.StartActivityRQ;
import com.scrumly.eventservice.dto.statistic.ActivityUserStatistic;
import com.scrumly.eventservice.enums.ActivityStatus;
import com.scrumly.eventservice.feign.UserServiceFeignClient;
import com.scrumly.eventservice.mapper.BusinessMapper;
import com.scrumly.eventservice.repository.ActivityEntityRepository;
import com.scrumly.eventservice.services.ActivityService;
import com.scrumly.eventservice.services.ActivityTemplateService;
import com.scrumly.eventservice.services.EventService;
import com.scrumly.eventservice.services.WorkspaceService;
import com.scrumly.exceptions.types.EntityNotFoundException;
import com.scrumly.exceptions.types.ServiceErrorException;
import com.scrumly.specification.CompareOption;
import com.scrumly.specification.CustomSearchOperators;
import com.scrumly.specification.GeneralSpecification;
import com.scrumly.specification.PageDto;
import com.scrumly.specification.SearchFilter;
import com.scrumly.specification.SearchOperators;
import com.scrumly.specification.SearchQuery;
import com.scrumly.specification.SortOrder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.scrumly.eventservice.utils.SecurityUtils.getUsername;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityEntityRepository activityRepository;
    private final EventService eventService;
    private final UserServiceFeignClient userServiceFeignClient;
    private final ActivityTemplateService templateService;
    private final ModelMapper modelMapper;
    private final BusinessMapper businessMapper;
    private final WorkspaceService workspaceService;


    @Override
    public ActivityDto startActivity(StartActivityRQ startActivityRQ) {
        if (!userServiceFeignClient.hasPermission(PermissionType.SCHEDULE_ACTIVITY,
                                                  Collections.singletonList(startActivityRQ.getTeamId()))) {
            throw new ServiceErrorException("You are not allowed to start activity");
        }
        ActivityEntity activityEntity = activityRepository.getByActivityId(startActivityRQ.getActivityId());
        if (activityEntity == null) {
            activityEntity = ActivityEntity.builder()
                    .activityId(UUID.randomUUID().toString())
                    .teamId(startActivityRQ.getTeamId())
                    .createdAt(LocalDateTime.now())
                    .status(ActivityStatus.CREATED)
                    .activityTemplate(templateService.getActivityTemplate(startActivityRQ.getTemplateId()))
                    .historyLog(Arrays.asList(
                            ActivityHistory.builder()
                                    .dateTime(LocalDateTime.now())
                                    .performedBy(getUsername())
                                    .previousStatus(null)
                                    .newStatus(ActivityStatus.CREATED)
                                    .changeDetails("New activity created")
                                    .build()
                    ))
                    .build();
            activityEntity = activityRepository.save(activityEntity);
        }
        return businessMapper.activityToDto(activityEntity);
    }

    @Override
    @Transactional
    public List<ActivityDto> scheduleActivity(CreateActivityRQ createActivityRQ) {
        if (!userServiceFeignClient.hasPermission(PermissionType.SCHEDULE_ACTIVITY,
                                                  Collections.singletonList(createActivityRQ.getEventDto().getCreatedFor()))) {
            throw new ServiceErrorException("You are not allowed to schedule activity");
        }
        List<EventEntity> events = new ArrayList<>();
        List<ActivityEntity> activities = new ArrayList<>();
        WorkspaceEntity workspace = workspaceService.createWorkspace();
        try {
            ActivityTemplateEntity template = null;
            if (createActivityRQ.getTemplateId() != null) {
                template = templateService.getActivityTemplate(createActivityRQ.getTemplateId());
            }
            events = eventService.scheduleEvent(createActivityRQ);
            for (EventEntity event : events) {
                ActivityEntity activityEntity = ActivityEntity.builder()
                        .activityId(UUID.randomUUID().toString())
                        .teamId(createActivityRQ.getEventDto().getCreatedFor())
                        .createdAt(LocalDateTime.now())
                        .createdBy(getUsername())
                        .status(ActivityStatus.SCHEDULED)
                        .scheduledEvent(event)
                        .recurringEventId(event.getRecurrence() != null
                                                  ? event.getRecurrence().getRecurringEventId()
                                                  : null)
                        .historyLog(Arrays.asList(
                                ActivityHistory.builder()
                                        .dateTime(LocalDateTime.now())
                                        .performedBy(getUsername())
                                        .previousStatus(null)
                                        .newStatus(ActivityStatus.SCHEDULED)
                                        .changeDetails("New activity scheduled")
                                        .build()
                        ))
                        .build();
                if (template != null) {
                    activityEntity.setActivityTemplate(template);
                }
                if (workspace != null) {
                    activityEntity.setWorkspace(workspace);
                }
                activities.add(activityEntity);
            }

            activities = activityRepository.saveAll(activities);

            if (workspace != null) {
                workspaceService.appendActivities(workspace.getWorkspaceId(), activities);
            }
        } catch (Exception e) {
            handleActivityScheduleError(events, workspace);
            e.printStackTrace();
            throw new ServiceErrorException(e);
        }
        return activities.stream()
                .map(businessMapper::activityToDto)
                .toList();
    }

    private void handleActivityScheduleError(List<EventEntity> events, WorkspaceEntity workspace) {
        if (events != null && !events.isEmpty()) {
            eventService.cancelEvents(events.stream()
                                              .map(EventEntity::getEventId)
                                              .collect(Collectors.toList()));
        }
        if (workspace != null) {
            workspaceService.deleteWorkspace(workspace);
        }
    }

    @Override
    @Transactional
    public ActivityDto rescheduleActivity(String activityId, CreateActivityRQ rq) {
        if (!userServiceFeignClient.hasPermission(PermissionType.SCHEDULE_ACTIVITY,
                                                  Collections.singletonList(rq.getEventDto().getCreatedFor()))) {
            throw new ServiceErrorException("You are not allowed to schedule activity");
        }
        ActivityEntity activityEntity = activityRepository.findByActivityId(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Activity is not found"));
        if (!activityEntity.getScheduledEvent().getCreatedBy().equals(getUsername())) {
            throw new ServiceErrorException("You are not allowed to modify this event");
        }

        if (rq.getTemplateId() != null && !rq.getTemplateId().isEmpty()) {
            activityEntity.setActivityTemplate(templateService.getActivityTemplate(rq.getTemplateId()));
        }
        activityRepository.save(activityEntity);
        try {
            if (activityEntity.getScheduledEvent().getEventMetadata() == null && (rq.isCreateConference() || rq.isCreateCalendarEvent())) {
                eventService.scheduleCalendarEvent(activityEntity.getScheduledEvent().getEventId(), rq);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        eventService.updateEvent(activityEntity.getScheduledEvent().getEventId(), rq.getEventDto());
        return getActivityById(activityId);
    }

    @Override
    public ActivityDto changeActivityStatus(String activityId, ActivityStatus status) {
        ActivityEntity activityEntity = activityRepository.findByActivityId(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Activity is not found"));
        if (!activityEntity.getScheduledEvent().getCreatedBy().equals(getUsername())) {
            throw new ServiceErrorException("You are not allowed to modify this event");
        }
        activityEntity.setStatus(status);
        activityRepository.save(activityEntity);
        return getActivityById(activityId);
    }

    @Override
    @Transactional
    public List<ActivityDto> rescheduleRecurrentActivity(String activityId, CreateActivityRQ rq) {
        if (!userServiceFeignClient.hasPermission(PermissionType.SCHEDULE_ACTIVITY,
                                                  Collections.singletonList(rq.getEventDto().getCreatedFor()))) {
            throw new ServiceErrorException("You are not allowed to reschedule activity");
        }
        ActivityEntity activityEntity = activityRepository.findByActivityId(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Activity is not found"));
        List<ActivityEntity> activities = activityRepository.findAllByRecurringEventId(activityEntity.getRecurringEventId());
        List<ActivityDto> rescheduled = new ArrayList<>();
        for (ActivityEntity activity : activities) {
            CreateActivityRQ newRq = new CreateActivityRQ(rq);
            EventDto eventDto = newRq.getEventDto();

            swapTime(activity, eventDto);

            rescheduled.add(rescheduleActivity(activity.getActivityId(), newRq));
        }
        return rescheduled;
    }

    private static void swapTime(ActivityEntity activity, EventDto eventDto) {
        ZonedDateTime originalStart = ZonedDateTime.parse(activity.getScheduledEvent().getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME);
        ZonedDateTime originalEnd = ZonedDateTime.parse(activity.getScheduledEvent().getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME);

        ZonedDateTime dtoStart = ZonedDateTime.parse(eventDto.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME);
        ZonedDateTime dtoEnd = ZonedDateTime.parse(eventDto.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME);

        LocalDate originalStartDate = originalStart.toLocalDate();
        LocalDate originalEndDate = originalEnd.toLocalDate();

        LocalTime newStartTime = dtoStart.toLocalTime();
        LocalTime newEndTime = dtoEnd.toLocalTime();

        ZonedDateTime newStart = ZonedDateTime.of(originalStartDate, newStartTime, originalStart.getZone());
        ZonedDateTime newEnd = ZonedDateTime.of(originalEndDate, newEndTime, originalEnd.getZone());

        eventDto.setStartDateTime(newStart.format(DateTimeFormatter.ISO_DATE_TIME));
        eventDto.setEndDateTime(newEnd.format(DateTimeFormatter.ISO_DATE_TIME));

        eventDto.setStartTimeZone(newStart.getZone().toString());
        eventDto.setEndTimeZone(newEnd.getZone().toString());
    }

    @Override
    public ActivityDto scheduleActivityCalendarEvent(ScheduleActivityCalendarEventRQ rq) {
        ActivityEntity activityEntity = activityRepository.findByActivityId(rq.getActivityId())
                .orElseThrow(() -> new EntityNotFoundException("Activity is not found"));
        if (activityEntity.getScheduledEvent().getEventMetadata() != null) {
            throw new ServiceErrorException("Calendar event already scheduled");
        }
        if (!activityEntity.getScheduledEvent().getCreatedBy().equals(getUsername())) {
            throw new ServiceErrorException("You are not allowed to modify this event");
        }
        if (!userServiceFeignClient.hasPermission(PermissionType.SCHEDULE_ACTIVITY,
                                                  Collections.singletonList(activityEntity.getScheduledEvent().getCreatedFor()))) {
            throw new ServiceErrorException("You are not allowed to schedule activity");
        }

        EventDto eventDto = modelMapper.map(activityEntity.getScheduledEvent(), EventDto.class);
        eventDto.setCreateConference(rq.getConferenceProvider() != null);
        CreateActivityRQ createActivityRQ = CreateActivityRQ.builder()
                .eventDto(eventDto)
                .conferenceProvider(rq.getConferenceProvider())
                .calendarProvider(rq.getCalendarProvider())
                .createCalendarEvent(rq.getCalendarProvider() != null)
                .createConference(rq.getConferenceProvider() != null)
                .build();
        eventService.scheduleCalendarEvent(eventDto.getEventId(), createActivityRQ);
        return getActivityById(activityEntity.getActivityId());
    }

    @Override
    public PageDto<ActivityDto> findActivities(SearchQuery searchQuery) {
        Specification<ActivityEntity> specification = GeneralSpecification.bySearchQuery(searchQuery);
        PageRequest pageable = GeneralSpecification.getPageRequest(searchQuery);
        Page<ActivityEntity> page = activityRepository.findAll(specification, pageable);
        return GeneralSpecification.getPageResponse(page, businessMapper::activityToDtoWithoutActivity);
    }

    @Override
    public ActivityDto getActivityById(String activityId) {
        ActivityEntity activityEntity = activityRepository.findByActivityId(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Activity is not found"));
        return businessMapper.activityToDtoWithoutWorkspace(activityEntity);
    }

    @Override
    public List<ActivityDto> getRecentTemplateActivities(String teamId, String templateId) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.appendSearchFilter(SearchFilter.builder()
                                               .property("teamId")
                                               .value(teamId)
                                               .compareOption(CompareOption.AND)
                                               .operator(SearchOperators.EQUALS.name())
                                               .build());
        searchQuery.appendSearchFilter(SearchFilter.builder()
                                               .property("activityTemplate.templateId")
                                               .value(templateId)
                                               .compareOption(CompareOption.AND)
                                               .operator(SearchOperators.EQUALS.name())
                                               .build());
        searchQuery.appendSearchFilter(SearchFilter.builder()
                                               .property("status")
                                               .value(com.scrumly.enums.events.ActivityStatus.FINISHED.toString())
                                               .compareOption(CompareOption.AND)
                                               .operator(CustomSearchOperators.IS_ACTIVITY_STATUS_EQUAL.name())
                                               .build());
        searchQuery.setSortOrder(SortOrder.builder()
                                         .descendingOrder(List.of("scheduledEvent.startDateTime"))
                                         .build());
        PageDto<ActivityDto> activities = findActivities(searchQuery);
        return activities.getData();
    }

    @Override
    @Transactional
    public void deleteActivity(String activityId) {
        ActivityEntity activityEntity = activityRepository.findByActivityId(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Activity is not found"));
        if (!activityEntity.getScheduledEvent().getCreatedBy().equals(getUsername())) {
            throw new ServiceErrorException("You are not allowed to modify this activity");
        }
        if (!userServiceFeignClient.hasPermission(PermissionType.SCHEDULE_ACTIVITY,
                                                  Collections.singletonList(activityEntity.getScheduledEvent().getCreatedFor()))) {
            throw new ServiceErrorException("You are not allowed to schedule activity");
        }
        if (!activityEntity.getStatus().equals(ActivityStatus.CANCELED)) {
            throw new ServiceErrorException("Activity is not canceled");
        }
        if (activityEntity.getScheduledEvent() != null) {
            eventService.deleteEvent(activityEntity.getScheduledEvent().getEventId());
        }
        if (activityEntity.getRecurringEventId() != null && activityEntity.getRecurringEventId().isEmpty()) {
            workspaceService.deleteWorkspaceActivity(activityEntity.getWorkspace().getWorkspaceId(), activityEntity.getActivityId());
        } else {
            workspaceService.deleteWorkspace(activityEntity.getWorkspace());
        }
        activityRepository.delete(activityEntity);
    }

    @Override
    @Transactional
    public void deleteAllRecurrentActivity(String recurrentEventId) {
        List<ActivityEntity> activities = activityRepository.findAllByRecurringEventIdAndStatus(recurrentEventId, ActivityStatus.CANCELED);
        if (activities != null && !activities.isEmpty()) {
            List<String> eventIds = activities.stream()
                    .filter(activityEntity -> activityEntity.getScheduledEvent() != null)
                    .map(activityEntity -> activityEntity.getScheduledEvent().getEventId())
                    .toList();
            ActivityEntity activityEntity = activities.get(0);
            eventService.deleteEvents(eventIds);
            workspaceService.deleteWorkspace(activityEntity.getWorkspace());
            activityRepository.deleteAll(activities);
        }
    }

    @Override
    @Transactional
    public void cancelActivity(String activityId) {
        ActivityEntity activityEntity = activityRepository.findByActivityId(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Activity is not found"));
        if (!activityEntity.getScheduledEvent().getCreatedBy().equals(getUsername())) {
            throw new ServiceErrorException("You are not allowed to modify this activity");
        }
        if (!userServiceFeignClient.hasPermission(PermissionType.SCHEDULE_ACTIVITY,
                                                  Collections.singletonList(activityEntity.getScheduledEvent().getCreatedFor()))) {
            throw new ServiceErrorException("You are not allowed to schedule activity");
        }

        activityEntity.setStatus(ActivityStatus.CANCELED);
        activityEntity.getHistoryLog().add(ActivityHistory.builder()
                                                   .dateTime(LocalDateTime.now())
                                                   .performedBy(getUsername())
                                                   .previousStatus(activityEntity.getStatus())
                                                   .newStatus(ActivityStatus.CANCELED)
                                                   .changeDetails("Activity canceled")
                                                   .build());
        activityRepository.save(activityEntity);
        eventService.cancelEvent(activityEntity.getScheduledEvent().getEventId());

        deleteActivity(activityId);
    }

    @Override
    @Transactional
    public void cancelAllRecurrentActivity(String recurrentEventId) {
        List<ActivityEntity> activities = activityRepository.findAllByRecurringEventId(recurrentEventId);
        if (activities == null || activities.isEmpty()) {
            throw new ServiceErrorException("No recurrent events found");
        }
        ActivityEntity activity = activities.get(0);
        if (!activity.getScheduledEvent().getCreatedBy().equals(getUsername())) {
            throw new ServiceErrorException("You are not allowed to modify this activity");
        }
        if (!userServiceFeignClient.hasPermission(PermissionType.SCHEDULE_ACTIVITY,
                                                  Collections.singletonList(activity.getScheduledEvent().getCreatedFor()))) {
            throw new ServiceErrorException("You are not allowed to schedule activity");
        }

        for (ActivityEntity activityEntity : activities) {
            activityEntity.setStatus(ActivityStatus.CANCELED);
            activityEntity.getHistoryLog().add(ActivityHistory.builder()
                                                       .dateTime(LocalDateTime.now())
                                                       .performedBy(getUsername())
                                                       .previousStatus(activityEntity.getStatus())
                                                       .newStatus(ActivityStatus.CANCELED)
                                                       .changeDetails("Activity canceled")
                                                       .build());
        }
        activityRepository.saveAll(activities);
        eventService.cancelAllRecurrentEvent(activity.getRecurringEventId());

        deleteAllRecurrentActivity(recurrentEventId);
    }

    @Override
    public void saveRoomReference(String activityId, String roomReference) {
        ActivityEntity activityEntity = activityRepository.findByActivityId(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Activity is not found"));
        activityEntity.setRoomId(roomReference);
        activityRepository.save(activityEntity);
    }

    @Override
    public ActivityUserStatistic getActivityStatistic(String username) {
        return null;
    }
}
