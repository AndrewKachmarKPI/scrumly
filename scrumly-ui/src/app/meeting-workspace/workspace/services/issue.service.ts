import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../enviroments/enviroment';
import { IssueShortInfo } from '../model/issues.model';
import { IntegrationServiceType } from '../../../member-dashboard/models/integration.model';

@Injectable({
  providedIn: 'root'
})
export class IssueService {

  constructor(private http: HttpClient) {
  }


  searchIssues(serviceType: IntegrationServiceType, connectingId: string, query: string): Observable<IssueShortInfo[]> {
    const url = `${ environment.api_url }/room/api/issues/search`
    return this.http.get<IssueShortInfo[]>(url, {
      params: {
        serviceType: serviceType,
        connectingId: connectingId,
        query: query
      }
    });
  }

  loadTopIssues(serviceType: IntegrationServiceType, connectingId: string, topLimit: number): Observable<IssueShortInfo[]> {
    const url = `${ environment.api_url }/room/api/issues/top`
    return this.http.get<IssueShortInfo[]>(url, {
      params: {
        serviceType: serviceType,
        connectingId: connectingId,
        topLimit: topLimit
      }
    });
  }
}
