import { Injectable } from '@angular/core';
import { environment } from "../../../../../enviroments/enviroment";
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, Observable } from "rxjs";
import { PageDto, SearchQuery } from "../../../../ui-components/models/search-filter.model";
import { CreateActivityRQ, ScheduleActivityCalendarEventRQ } from "../model/event.model";
import { ActivityDto, FindTimeSlotRQ, TimeSlotGroupDto } from "../model/activity.model";

@Injectable({
  providedIn: 'root'
})
export class ActivityService {
  onEventCreatedSource = new BehaviorSubject(false);
  onEventCreated = this.onEventCreatedSource.asObservable();

  private apiUrl = `${ environment.api_url }/events/api/activities`;

  constructor(private http: HttpClient) {
  }

  startActivity(request: CreateActivityRQ): Observable<ActivityDto> {
    return this.http.post<ActivityDto>(this.apiUrl + '/start', request);
  }

  scheduleActivity(request: CreateActivityRQ): Observable<ActivityDto[]> {
    return this.http.post<ActivityDto[]>(this.apiUrl, request);
  }

  scheduleActivityCalendarEvent(request: ScheduleActivityCalendarEventRQ): Observable<ActivityDto> {
    return this.http.post<ActivityDto>(this.apiUrl + '/schedule/event', request);
  }

  reScheduleActivity(activityId: string, request: CreateActivityRQ): Observable<ActivityDto> {
    return this.http.post<ActivityDto>(this.apiUrl + `/${ activityId }/reschedule`, request);
  }

  reScheduleRecurrentActivity(activityId: string, request: CreateActivityRQ): Observable<ActivityDto> {
    return this.http.post<ActivityDto>(this.apiUrl + `/${ activityId }/reschedule/recurrent`, request);
  }

  findActivities(query: SearchQuery): Observable<PageDto<ActivityDto>> {
    return this.http.post<PageDto<ActivityDto>>(this.apiUrl + '/all', query);
  }

  getActivityById(activityId: string): Observable<ActivityDto> {
    return this.http.get<ActivityDto>(`${ this.apiUrl }/${ activityId }`);
  }

  deleteActivity(activityId: string): Observable<void> {
    return this.http.delete<void>(`${ this.apiUrl }/${ activityId }`);
  }

  cancelActivity(activityId: string): Observable<void> {
    return this.http.delete<void>(`${ this.apiUrl }/${ activityId }/cancel`);
  }

  deleteAllRecurrentActivity(recurrentEventId: string): Observable<void> {
    return this.http.delete<void>(`${ this.apiUrl }/${ recurrentEventId }/recurrent`);
  }

  cancelAllRecurrentActivity(recurrentEventId: string): Observable<void> {
    return this.http.delete<void>(`${ this.apiUrl }/${ recurrentEventId }/cancel/recurrent`);
  }

  findTimeSlotRQ(findTimeSlotRQ: FindTimeSlotRQ): Observable<TimeSlotGroupDto[]> {
    return this.http.post<TimeSlotGroupDto[]>(`${ this.apiUrl }/slot/availability`, findTimeSlotRQ);
  }
}
