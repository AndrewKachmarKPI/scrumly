import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { environment } from "../../../../../enviroments/enviroment";
import { filter, Observable } from "rxjs";
import { IntegrationServiceType } from "../../../models/integration.model";
import { switchMap } from "rxjs/operators";
import { MessageService } from "primeng/api";
import { TranslateService } from "@ngx-translate/core";
import { openOauthIntegrationPopup } from "../../../../ui-components/services/utils";

@Injectable({
  providedIn: 'root'
})
export class GoogleCalendarService {
  constructor(private httpClient: HttpClient,
              private messageService: MessageService,
              private translateService: TranslateService) {
  }


  getAuthorizationUrl() {
    const url = `${ environment.api_url }/integrations/google/calendar/authorize`
    return this.httpClient.get(url, {
      responseType: 'text'
    });
  }

  authorize(code: string) {
    const url = `${ environment.api_url }/integrations/google/calendar/authorize`
    return this.httpClient.post(url, {}, {
      params: {
        code: code
      }
    });
  }

  getCalendarEvents(): Observable<any> {
    const url = `${ environment.api_url }/integrations/google/calendar/events`
    return this.httpClient.get(url);
  }


  initAuthorization(): Observable<boolean> {
    return new Observable(observer => {
      this.getAuthorizationUrl().pipe(
        switchMap(url => openOauthIntegrationPopup(url, IntegrationServiceType.GOOGLE_CALENDAR)),
        filter(Boolean),
        switchMap(code => this.authorize(code))
      ).subscribe({
        next: () => {
          observer.next(true);
          this.messageService.add({
            severity: 'success',
            summary: this.translateService.instant(`messages.success.${ IntegrationServiceType.GOOGLE_CALENDAR }.connection.summary`),
            detail: this.translateService.instant(`messages.success.${ IntegrationServiceType.GOOGLE_CALENDAR }.connection.detail`),
          });
        },
        error: (err) => {
          observer.next(false);
          this.messageService.add({
            severity: 'error',
            summary: this.translateService.instant(`messages.error.${ IntegrationServiceType.GOOGLE_CALENDAR }.connection.summary`),
            detail: this.translateService.instant(`messages.error.${ IntegrationServiceType.GOOGLE_CALENDAR }.connection.detail`),
          });
        }
      });
    })
  }
}
