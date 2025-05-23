import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../enviroments/enviroment';
import { ActivityRoomReport, FileDocument } from '../model/activity-room-report.model';

@Injectable({
  providedIn: 'root'
})
export class ActivityRoomReportService {

  constructor(private http: HttpClient) {
  }

  getActivityRoomReport(activityId: string): Observable<ActivityRoomReport> {
    const url = `${ environment.api_url }/room/api/activities/rooms/reports/${ activityId }`
    return this.http.get<ActivityRoomReport>(url, {});
  }

  getActivityRoomReportDocument(activityId: string): Observable<FileDocument> {
    const url = `${ environment.api_url }/room/api/activities/rooms/reports/${ activityId }/document`
    return this.http.get<FileDocument>(url);
  }

  getActivityRoomReportDocumentView(activityId: string): Observable<Blob> {
    const url = `${ environment.api_url }/room/api/activities/rooms/reports/${ activityId }/document/view`
    return this.http.get(url, { responseType: 'blob' });
  }


  test(activityId: string): Observable<ActivityRoomReport> {
    const url = `${ environment.api_url }/room/api/activities/rooms/reports/${ activityId }/test`
    return this.http.get<ActivityRoomReport>(url, {});
  }
}
