import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { IssueDto, IssueExportOption } from '../model/backlog.model';
import { Observable } from 'rxjs';
import { PageDto, SearchQuery } from '../../../../ui-components/models/search-filter.model';
import { environment } from '../../../../../enviroments/enviroment';

@Injectable({
  providedIn: 'root'
})
export class IssueService {

  private apiUrl = `${ environment.api_url }/room/api/issues`;

  constructor(private http: HttpClient) {
  }

  // Get all issues with pagination and search
  searchIssues(searchQuery: SearchQuery): Observable<PageDto<IssueDto>> {
    return this.http.post<PageDto<IssueDto>>(`${ this.apiUrl }/search`, searchQuery);
  }

  // Get an issue by its ID
  getIssueById(id: number): Observable<IssueDto> {
    return this.http.get<IssueDto>(`${ this.apiUrl }/${ id }`);
  }

  getIssueByKey(key: string): Observable<IssueDto> {
    return this.http.get<IssueDto>(`${ this.apiUrl }/${ key }/key`);
  }

  // Create a new issue
  createIssue(issueDto: IssueDto): Observable<IssueDto> {
    return this.http.post<IssueDto>(this.apiUrl, issueDto);
  }

  // Update an existing issue
  updateIssue(id: number, issueDto: IssueDto): Observable<IssueDto> {
    return this.http.put<IssueDto>(`${ this.apiUrl }/${ id }`, issueDto);
  }

  // Delete an issue by its ID
  deleteIssue(id: number): Observable<void> {
    return this.http.delete<void>(`${ this.apiUrl }/${ id }`);
  }

  archiveIssue(id: number): Observable<void> {
    return this.http.delete<void>(`${ this.apiUrl }/${ id }/archive`);
  }

  getIssueExportOptions(issueKey: string): Observable<IssueExportOption[]> {
    return this.http.get<IssueExportOption[]>(`${ this.apiUrl }/${ issueKey }/export`);
  }

  exportIssue(issueKey: string, description:string, exportOption: IssueExportOption[]): Observable<IssueDto> {
    return this.http.post<IssueDto>(`${ this.apiUrl }/${issueKey }/export`, exportOption, {
      params: {
        description: description
      }
    });
  }
}
