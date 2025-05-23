import { Injectable } from '@angular/core';
import { environment } from '../../../../../enviroments/enviroment';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { PageDto, SearchQuery } from '../../../../ui-components/models/search-filter.model';
import { CreateActivityRQ, ScheduleActivityCalendarEventRQ } from '../model/event.model';
import { ActivityDto, FindTimeSlotRQ, TimeSlotGroupDto } from '../model/activity.model';
import { ActivityUserStatistic } from '../model/activity-statistic.model';

@Injectable({
  providedIn: 'root'
})
export class ActivityStatisticService {
  private apiUrl = `${ environment.api_url }/events/api/activities/statistic`;

  constructor(private http: HttpClient) {
  }

  getActivityUserStatistic(userId: string): Observable<ActivityUserStatistic> {
    return this.http.get<ActivityUserStatistic>(this.apiUrl + `/${ userId }/user`);
  }

  getMyActivityUserStatistic(dates: string[]): Observable<ActivityUserStatistic> {
    const options = dates && dates.length > 0 ? {
      params: {
        startDate: dates[0]!,
        endDate: dates[1]!
      }
    } : {};
    return this.http.get<ActivityUserStatistic>(this.apiUrl + `/me`, options);
  }

}
