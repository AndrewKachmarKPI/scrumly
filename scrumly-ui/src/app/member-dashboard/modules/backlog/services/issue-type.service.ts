import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IssueStatusDto, IssueTypeDto } from '../model/backlog.model';
import { environment } from '../../../../../enviroments/enviroment';

@Injectable({
  providedIn: 'root',
})
export class IssueTypeService {
  private apiUrl = `${ environment.api_url }/room/api/issue-types`;

  constructor(private http: HttpClient) {
  }

  getAllIssueTypes(backlogId: string): Observable<IssueTypeDto[]> {
    return this.http.get<IssueTypeDto[]>(`${ this.apiUrl }/${ backlogId }/all`);
  }

  // Get issue type by its ID
  getIssueTypeById(id: number): Observable<IssueTypeDto> {
    return this.http.get<IssueTypeDto>(`${ this.apiUrl }/${ id }`);
  }

  // Create a new issue type
  createIssueType(issueTypeDto: IssueTypeDto): Observable<IssueTypeDto> {
    return this.http.post<IssueTypeDto>(this.apiUrl, issueTypeDto);
  }

  // Update an existing issue type
  updateIssueType(id: number, issueTypeDto: IssueTypeDto): Observable<IssueTypeDto> {
    return this.http.put<IssueTypeDto>(`${ this.apiUrl }/${ id }`, issueTypeDto);
  }

  // Delete an issue type by its ID
  deleteIssueType(id: number): Observable<void> {
    return this.http.delete<void>(`${ this.apiUrl }/${ id }`);
  }
}
