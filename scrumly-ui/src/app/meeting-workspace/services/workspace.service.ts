import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, filter, Observable, Subject, takeUntil } from 'rxjs';
import { UserInfoDto } from '../../auth/auth.model';
import { environment } from '../../../enviroments/enviroment';
import { WorkspaceDto } from '../../member-dashboard/modules/events/model/workspace.model';
import { MessageService } from 'primeng/api';
import { ActivityDto } from '../../member-dashboard/modules/events/model/activity.model';

@Injectable({
  providedIn: 'root'
})
export class WorkspaceService implements OnDestroy {
  destroy$: Subject<void> = new Subject();


  constructor(private http: HttpClient) {
  }


  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }


  // HTTP METHODS

  findWorkspace(workspaceId: string): Observable<WorkspaceDto> {
    const url = `${ environment.api_url }/events/api/workspaces/${ workspaceId }`
    return this.http.get<WorkspaceDto>(url);
  }

  createConference(workspaceId: string): Observable<WorkspaceDto> {
    const url = `${ environment.api_url }/events/api/workspaces/${ workspaceId }`
    return this.http.put<WorkspaceDto>(url, {});
  }
}
