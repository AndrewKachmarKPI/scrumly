import { Component, OnDestroy, OnInit } from '@angular/core';
import { filter, Observable, Subject, takeUntil } from 'rxjs';
import { IntegrationServiceDto } from '../../../models/integration.model';
import { IntegrationService } from '../../integration/services/integration.service';
import { ActivatedRoute } from '@angular/router';
import { JiraCloudService } from '../../integration/services/jira-cloud.service';

@Component({
  selector: 'app-org-integrations',
  templateUrl: './org-integrations.component.html',
  styleUrl: './org-integrations.component.css'
})
export class OrgIntegrationsComponent implements OnInit, OnDestroy {
  orgId?: string;
  integrationServices!: Observable<IntegrationServiceDto[]>;
  destroy$ = new Subject<void>();

  constructor(private readonly integrationService: IntegrationService,
              private readonly activateRoute: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.activateRoute.params.subscribe(params => {
      this.orgId = params['orgId'];
      this.loadIntegrations();
    });
    this.integrationService.onServiceConnectionChange
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loadIntegrations();
        }
      })
  }


  loadIntegrations() {
    this.integrationServices = this.integrationService.findOrgIntegrationServices(this.orgId!);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
