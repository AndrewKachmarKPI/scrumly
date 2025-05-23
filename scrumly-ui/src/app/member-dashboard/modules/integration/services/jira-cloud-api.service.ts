import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { environment } from '../../../../../enviroments/enviroment';
import { Observable } from 'rxjs';
import { GetIssuePickerSuggestions } from '../model/jira-api.model';

@Injectable({
  providedIn: 'root'
})
export class JiraCloudApiService {

  private BASE_PATH = `${ environment.api_url }/integrations/api/jira/cloud`;

  constructor(private httpClient: HttpClient) {
  }

  getIssuePickerSuggestions(connectingId: string, query: string): Observable<GetIssuePickerSuggestions> {
    const url = `${ this.BASE_PATH }/issue/picker`
    return this.httpClient.get<GetIssuePickerSuggestions>(url, {
      params: {
        connectingId: connectingId,
        query: query
      }
    });
  }

}
