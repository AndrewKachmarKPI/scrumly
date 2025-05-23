import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, filter, Observable, of } from 'rxjs';
import {
  IntegrationCallback,
  IntegrationServiceDto,
  IntegrationServiceType,
  ServiceRefreshRQ
} from '../../../models/integration.model';
import { environment } from '../../../../../enviroments/enviroment';
import { GoogleCalendarService } from './google-calendar.service';
import { switchMap } from 'rxjs/operators';
import { MessageService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../../auth/auth.service';
import { JiraCloudService } from './jira-cloud.service';

@Injectable({
  providedIn: 'root'
})
export class IntegrationService {
  private myIntegratedServicesSubject = new BehaviorSubject<IntegrationServiceDto[]>([]);
  onMyIntegratedServicesChange: Observable<IntegrationServiceDto[]> = this.myIntegratedServicesSubject.asObservable();

  private serviceConnectionChangeSubject = new BehaviorSubject(false);
  onServiceConnectionChange = this.serviceConnectionChangeSubject.asObservable();

  private integrationCallbacks: Map<IntegrationServiceType, (connectionId?: string) => void> = new Map();

  constructor(private httpClient: HttpClient,
              private messageService: MessageService,
              private translateService: TranslateService,
              private googleCalendarService: GoogleCalendarService,
              private jiraCloudService: JiraCloudService) {
    this.loadIntegrationServices();
    this.integrationCallbacks.set(
      IntegrationServiceType.GOOGLE_CALENDAR, () => {
        this.googleCalendarService.initAuthorization().subscribe(response => {
          if (response) {
            this.loadIntegrationServices();
          }
        })
      },
    );
    this.integrationCallbacks.set(
      IntegrationServiceType.JIRA_CLOUD, (connectionId) => {
        this.jiraCloudService.initAuthorization(connectionId!).subscribe(response => {
          if (response) {
            this.serviceConnectionChangeSubject.next(true);
          }
        })
      }
    );
  }

  loadIntegrationServices() {
    this.findMyIntegrationServices().subscribe(services => {
      this.myIntegratedServicesSubject.next(services);
    });
  }

  getIntegrationCallback(serviceType: IntegrationServiceType) {
    return this.integrationCallbacks.get(serviceType);
  }

  findMyIntegrationServices(): Observable<IntegrationServiceDto[]> {
    const url = `${ environment.api_url }/integrations/services/me`
    return this.httpClient.get<IntegrationServiceDto[]>(url);
  }

  findOrgIntegrationServices(orgId: string): Observable<IntegrationServiceDto[]> {
    const url = `${ environment.api_url }/integrations/services/org/${ orgId }`
    return this.httpClient.get<IntegrationServiceDto[]>(url);
  }

  isConnected(connectionId: string, serviceType: IntegrationServiceType): Observable<boolean> {
    const url = `${ environment.api_url }/integrations/services/is-connected`
    return this.httpClient.get<boolean>(url, {
      params: {
        connectionId: connectionId,
        serviceType: serviceType.toString()
      }
    });
  }

  disconnectService(serviceType: IntegrationServiceType, connectionId?: string) {
    const url = `${ environment.api_url }/integrations/services`
    this.httpClient.delete<IntegrationServiceDto[]>(url, {
      params: {
        serviceName: serviceType,
        connectionId: connectionId!
      }
    }).subscribe({
      next: () => {
        this.loadIntegrationServices();
        this.serviceConnectionChangeSubject.next(false);
        this.messageService.add({
          severity: 'success',
          summary: this.translateService.instant(`messages.success.disconnection.summary`),
          detail: this.translateService.instant(`messages.success.disconnection.detail`),
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: this.translateService.instant(`messages.error.disconnection.summary`),
          detail: this.translateService.instant(`messages.error.disconnection.detail`),
        });
      }
    });
  }

  refreshServiceAccess(rq: ServiceRefreshRQ) {
    const url = `${ environment.api_url }/integrations/services`
    this.httpClient.put(url, rq).subscribe({
      next: () => {
        this.loadIntegrationServices();
        this.messageService.add({
          severity: 'success',
          summary: this.translateService.instant(`messages.success.refresh.summary`),
          detail: this.translateService.instant(`messages.success.refresh.detail`),
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: this.translateService.instant(`messages.error.refresh.summary`),
          detail: this.translateService.instant(`messages.error.refresh.detail`),
        });
      }
    });
  }
}
