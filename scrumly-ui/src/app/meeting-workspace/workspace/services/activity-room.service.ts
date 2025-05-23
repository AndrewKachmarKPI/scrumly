import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../enviroments/enviroment';
import { Observable } from 'rxjs';
import { ActivityRoom, SyncBlockOption } from '../model/activity-room.model';
import { ActivityRoomReport } from '../model/activity-room-report.model';

@Injectable({
  providedIn: 'root'
})
export class ActivityRoomService {

  constructor(private http: HttpClient) {
  }

  createActivityRoom(activityId: string): Observable<ActivityRoom> {
    const url = `${ environment.api_url }/room/api/activities/rooms/${ activityId }`
    return this.http.post<ActivityRoom>(url, {});
  }

  finishActivityRoom(activityId: string): Observable<ActivityRoom> {
    const url = `${ environment.api_url }/room/api/activities/rooms/${ activityId }/finish`
    return this.http.post<ActivityRoom>(url, {});
  }

  getActivityRoom(activityId: string): Observable<ActivityRoom> {
    const url = `${ environment.api_url }/room/api/activities/rooms/${ activityId }`
    return this.http.get<ActivityRoom>(url, {});
  }

  isActivityRoomCreated(activityId: string): Observable<boolean> {
    const url = `${ environment.api_url }/room/api/activities/rooms/${ activityId }/exists`
    return this.http.get<boolean>(url, {});
  }

  joinRoom(activityId: string) {
    const url = `${ environment.api_url }/room/api/activities/rooms/${ activityId }/join`
    return this.http.post<ActivityRoom>(url, {});
  }

  exitRoom(activityId: string) {
    const url = `${ environment.api_url }/room/api/activities/rooms/${ activityId }/exit`
    return this.http.post<ActivityRoom>(url, {});
  }


  getSyncBlockOptions(activityId: string) {
    const url = `${ environment.api_url }/room/api/activities/rooms/${ activityId }/sync-block`
    return this.http.get<SyncBlockOption[]>(url, {});
  }


  test(activityId: string): Observable<ActivityRoomReport> {
    const url = `${ environment.api_url }/room/api/activities/rooms/${ activityId }/testExport`
    return this.http.post<ActivityRoomReport>(url, {});
  }
}
