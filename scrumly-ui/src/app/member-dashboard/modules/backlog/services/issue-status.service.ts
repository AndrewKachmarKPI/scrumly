import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BacklogIssueStatusesDto, IssueStatusDto } from '../model/backlog.model';
import { environment } from '../../../../../enviroments/enviroment';

@Injectable({
  providedIn: 'root',
})
export class IssueStatusService {
  private apiUrl = `${ environment.api_url }/room/api/issue-statuses`;

  constructor(private http: HttpClient) {
  }

  // Get all issue statuses
  getAllIssueStatuses(backlogId: string): Observable<IssueStatusDto[]> {
    return this.http.get<IssueStatusDto[]>(`${ this.apiUrl }/${ backlogId }/all`);
  }

  getTeamAllIssueStatuses(teamId: string): Observable<BacklogIssueStatusesDto[]> {
    return this.http.get<BacklogIssueStatusesDto[]>(`${ this.apiUrl }/team/${ teamId }/all`);
  }

  // Get issue status by its ID
  getIssueStatusById(id: number): Observable<IssueStatusDto> {
    return this.http.get<IssueStatusDto>(`${ this.apiUrl }/${ id }`);
  }

  // Create a new issue status
  createIssueStatus(issueStatusDto: IssueStatusDto): Observable<IssueStatusDto> {
    return this.http.post<IssueStatusDto>(this.apiUrl, issueStatusDto);
  }

  // Update an existing issue status
  updateIssueStatus(id: number, issueStatusDto: IssueStatusDto): Observable<IssueStatusDto> {
    return this.http.put<IssueStatusDto>(`${ this.apiUrl }/${ id }`, issueStatusDto);
  }

  // Delete an issue status by its ID
  deleteIssueStatus(id: number): Observable<void> {
    return this.http.delete<void>(`${ this.apiUrl }/${ id }`);
  }
}
