import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../../../../enviroments/enviroment';
import { BacklogDto } from '../model/backlog.model';
import { UserProfileDto } from '../../../../auth/auth.model';

@Injectable({
  providedIn: 'root'
})
export class BacklogService {
  backlogSelectionSource: BehaviorSubject<BacklogDto | undefined> = new BehaviorSubject<BacklogDto | undefined>(undefined);
  onBacklogSelection = this.backlogSelectionSource.asObservable();


  constructor(private http: HttpClient) {
    const backlog = localStorage.getItem('selectedBacklog');
    if (backlog) {
      this.backlogSelectionSource.next(JSON.parse(backlog!));
    }
  }

  selectBacklog(backlog?: BacklogDto) {
    this.backlogSelectionSource.next(backlog);
    if (backlog) {
      const copy: BacklogDto = JSON.parse(JSON.stringify(backlog!));
      copy.issues = [];
      localStorage.setItem('selectedBacklog', JSON.stringify(copy!));
    } else {
      localStorage.removeItem('selectedBacklog');
    }
  }

  // HTTP METHODS
  getTeamBacklogs(teamId: string): Observable<BacklogDto[]> {
    const url = `${ environment.api_url }/room/api/backlogs/${ teamId }/all`
    return this.http.get<BacklogDto[]>(url, {});
  }

  createDefaultBacklog(teamId: string): Observable<BacklogDto> {
    const url = `${ environment.api_url }/room/api/backlogs/${ teamId }/default`
    return this.http.post<BacklogDto>(url, {});
  }

  createBacklog(teamId: string, dto: BacklogDto): Observable<BacklogDto> {
    const url = `${ environment.api_url }/room/api/backlogs/${ teamId }`
    return this.http.post<BacklogDto>(url, dto);
  }

  hasBacklog(teamId: string): Observable<BacklogDto> {
    const url = `${ environment.api_url }/room/api/backlogs/${ teamId }/exists`
    return this.http.get<BacklogDto>(url, {});
  }

  getBacklogByTeamId(teamId: string): Observable<BacklogDto> {
    const url = `${ environment.api_url }/room/api/backlogs/${ teamId }`
    return this.http.get<BacklogDto>(url, {});
  }

  getAllAssignee(backlogId: string): Observable<UserProfileDto[]> {
    const url = `${ environment.api_url }/room/api/backlogs/${ backlogId }/assignee`
    return this.http.get<UserProfileDto[]>(url, {});
  }

}
