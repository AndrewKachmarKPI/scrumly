import { Component, Input, OnInit } from '@angular/core';
import { IntegrationService } from '../../services/integration.service';
import { IntegrationServiceDto, IntegrationServiceType } from '../../../../models/integration.model';
import { ConfirmationService, MenuItem } from 'primeng/api';


@Component({
  selector: 'integration-services-list',
  templateUrl: './integration-services-list.component.html',
  styleUrl: './integration-services-list.component.css'
})
export class IntegrationServicesListComponent implements OnInit {
  @Input() callbackParam?: string;
  @Input() services?: IntegrationServiceDto[];

  constructor(private readonly integrationService: IntegrationService,
              private readonly confirmationService: ConfirmationService) {
  }

  ngOnInit(): void {
  }

  connect(service: IntegrationServiceDto) {
    const callback = this.integrationService.getIntegrationCallback(service.serviceName);
    if (callback) {
      callback(this.callbackParam);
    }
  }

  onDisconnect(service: IntegrationServiceDto) {
    this.confirmationService.confirm({
      message: `Are you sure that you want to disconnect this service ?`,
      header: 'Disconnect service',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: 'none',
      rejectIcon: 'none',
      acceptButtonStyleClass: 'p-button-success',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.disconnect(service);
      }
    });
  }

  getServiceOptions(service: IntegrationServiceDto): MenuItem[] {
    if (service.serviceName === IntegrationServiceType.GOOGLE_CALENDAR) {
      return [
        {
          label: 'Disconnect Google Calendar',
          icon: 'pi pi-times',
          command: () => this.onDisconnect(service)
        }
      ];
    } else if (service.serviceName === IntegrationServiceType.JIRA_CLOUD) {
      return [
        {
          label: 'Refresh access',
          icon: 'pi pi-refresh',
          command: () => this.onRefresh(service)
        },
        {
          label: 'Disconnect Jira Cloud',
          icon: 'pi pi-times',
          command: () => this.onDisconnect(service)
        }
      ];
    }
    return [];
  }

  disconnect(service: IntegrationServiceDto) {
    this.integrationService.disconnectService(service.serviceName, service.connectionId);
  }

  onRefresh(service: IntegrationServiceDto) {
    this.integrationService.refreshServiceAccess({
      serviceType: service.serviceName,
      connectingId: service.connectionId
    });
  }
}
