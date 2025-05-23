import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  ActivityRoomNotesMetadata,
  ActivitySummaryNotes,
  BlockNavigationMetadata,
  ReflectColumnCards, ReflectColumnMetadata, UserColumnReflectCard,
  UserQuestionAnswer
} from '../model/activity-room.model';
import { environment } from '../../../../enviroments/enviroment';
import { IssueShortInfo } from '../model/issues.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ActivityRoomActionsService {
  private BASE_PATH = `${ environment.api_url }/room/api/activities/rooms/actions`;

  constructor(private http: HttpClient) {
  }


  onSelectEstimateIssues(activityId: string, blockId: string, issues: IssueShortInfo[]) {
    const url = `${ this.BASE_PATH }/${ activityId }/block/${ blockId }/onSelectEstimateIssues`;
    return this.http.post(url, issues);
  }

  onDeleteEstimateIssues(activityId: string, blockId: string, issueIds: string[]) {
    const url = `${ this.BASE_PATH }/${ activityId }/block/${ blockId }/onDeleteEstimateIssues`;
    return this.http.post(url, issueIds);
  }

  onUpdateEstimateIssues(activityId: string, blockId: string, issueIds: string[]) {
    const url = `${ this.BASE_PATH }/${ activityId }/block/${ blockId }/onUpdateEstimateIssues`;
    return this.http.post(url, issueIds);
  }

  onSelectBoardBacklogIssues(activityId: string, blockId: string, issues: IssueShortInfo[]) {
    const url = `${ this.BASE_PATH }/${ activityId }/block/${ blockId }/onSelectBoardBacklogIssues`;
    return this.http.post(url, issues);
  }

  onDeleteBoardBacklogIssues(activityId: string, blockId: string, issueIds: string[]) {
    const url = `${ this.BASE_PATH }/${ activityId }/block/${ blockId }/onDeleteBoardBacklogIssues`;
    return this.http.post(url, issueIds);
  }

  onGenerateNotes(activityId: string): Observable<ActivitySummaryNotes> {
    const url = `${ this.BASE_PATH }/${ activityId }/onGenerateNotes`;
    return this.http.post<ActivitySummaryNotes>(url, {});
  }

  onSetMeetingNotes(activityId: string, notes: string): Observable<ActivityRoomNotesMetadata> {
    const url = `${ this.BASE_PATH }/${ activityId }/onSetMeetingNotes`;
    return this.http.post<ActivityRoomNotesMetadata>(url, notes);
  }

  onGenerateColumnReflection(activityId: string, blockId: string, prompt: string, column: ReflectColumnMetadata): Observable<UserColumnReflectCard> {
    const url = `${ this.BASE_PATH }/${ activityId }/block/${ blockId }/onGenerateColumnReflection`;
    return this.http.post<UserColumnReflectCard>(url, column, {
      params: {
        prompt: prompt
      }
    });
  }
}
