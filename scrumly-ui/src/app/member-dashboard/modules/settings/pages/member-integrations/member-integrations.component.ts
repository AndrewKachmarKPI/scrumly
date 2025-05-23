import { Component, OnInit } from '@angular/core';
import { IntegrationService } from "../../../integration/services/integration.service";
import { Observable } from "rxjs";
import { IntegrationServiceDto } from "../../../../models/integration.model";
import { GoogleCalendarService } from "../../../integration/services/google-calendar.service";

@Component({
  selector: 'app-member-integrations',
  templateUrl: './member-integrations.component.html',
  styleUrl: './member-integrations.component.css'
})
export class MemberIntegrationsComponent implements OnInit {
  integrationServices!: Observable<IntegrationServiceDto[]>;

  constructor(private readonly integrationService: IntegrationService) {
  }

  ngOnInit(): void {
    this.integrationService.loadIntegrationServices();
    this.integrationServices = this.integrationService.onMyIntegratedServicesChange;
  }
}
