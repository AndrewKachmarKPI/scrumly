import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { MessageService } from "primeng/api";
import { TranslateService } from "@ngx-translate/core";
import { environment } from "../../../../../enviroments/enviroment";
import { filter, Observable } from "rxjs";
import { switchMap } from "rxjs/operators";
import { IntegrationServiceType } from "../../../models/integration.model";
import { openOauthIntegrationPopup } from "../../../../ui-components/services/utils";

@Injectable({
  providedIn: 'root'
})
export class JiraCloudService {

  constructor(private httpClient: HttpClient,
              private messageService: MessageService,
              private translateService: TranslateService) {
  }


  getAuthorizationUrl() {
    const url = `${ environment.api_url }/integrations/jira/cloud/authorize`
    return this.httpClient.get(url, {
      responseType: 'text'
    });
  }

  authorize(code: string, orgId: string) {
    const url = `${ environment.api_url }/integrations/jira/cloud/authorize`
    return this.httpClient.post(url, {}, {
      params: {
        code: code,
        orgId: orgId
      }
    });
  }

  initAuthorization(orgId: string): Observable<boolean> {
    return new Observable(observer => {
      this.getAuthorizationUrl().pipe(
        switchMap(url => openOauthIntegrationPopup(url, IntegrationServiceType.JIRA_CLOUD)),
        filter(Boolean),
        switchMap(code => this.authorize(code, orgId))
      ).subscribe({
        next: () => {
          observer.next(true);
          this.messageService.add({
            severity: 'success',
            summary: this.translateService.instant(`messages.success.${ IntegrationServiceType.JIRA_CLOUD }.connection.summary`),
            detail: this.translateService.instant(`messages.success.${ IntegrationServiceType.JIRA_CLOUD }.connection.detail`),
          });
        },
        error: (err) => {
          observer.next(false);
          this.messageService.add({
            severity: 'error',
            summary: this.translateService.instant(`messages.error.${ IntegrationServiceType.JIRA_CLOUD }.connection.summary`),
            detail: this.translateService.instant(`messages.error.${ IntegrationServiceType.JIRA_CLOUD }.connection.detail`),
          });
        }
      });
    })
  }

}
