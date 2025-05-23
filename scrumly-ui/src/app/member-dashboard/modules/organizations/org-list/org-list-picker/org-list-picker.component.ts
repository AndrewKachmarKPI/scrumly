import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { OrganizationService } from '../../services/organization.service';
import { AuthService } from '../../../../../auth/auth.service';
import { OrganizationConnectionDto } from '../../../../../auth/auth.model';
import { OrganizationInfoDto } from '../../model/organization.model';
import { filter, Subject, takeUntil } from 'rxjs';
import { OrgCreateDialogComponent } from '../../dialogs/org-create-dialog/org-create-dialog.component';
import { DialogService } from 'primeng/dynamicdialog';

@Component({
  selector: 'org-list-picker',
  templateUrl: './org-list-picker.component.html',
  styleUrl: './org-list-picker.component.css'
})
export class OrgListPickerComponent implements OnInit, OnDestroy {
  @Input() showHeader: boolean = false;
  @Input() showCreateButton: boolean = false;
  selectedOrganization?: OrganizationInfoDto;
  organizations: OrganizationInfoDto[] = [];
  connectedOrganizations: OrganizationConnectionDto[] = [];
  destroy$: Subject<void> = new Subject<void>();
  ids: string[] = [];

  constructor(private organizationService: OrganizationService,
              private dialogService: DialogService,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.onProfileUpdate
      .pipe(filter(Boolean), takeUntil(this.destroy$))
      .subscribe({
        next: (profile) => {
          this.connectedOrganizations = profile?.connectedOrganizations!;
          this.ids = this.connectedOrganizations.map(value => value.organizationId);
          this.loadOrganizationList(this.ids);
        }
      });
    this.organizationService.onOrganizationEventSubject
      .pipe(filter(Boolean), takeUntil(this.destroy$))
      .subscribe({
        next: (orgId) => {
          this.loadOrganizationList(this.ids);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }


  loadOrganizationList(ids: string[]) {
    if (ids && ids.length > 0) {
      this.organizationService.findOrganizationInfoList(ids)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (organizations) => {
            this.organizations = organizations;
            this.checkSelectionChange();
          }
        })
    }
  }


  checkSelectionChange() {
    this.organizationService.onOrganizationSelectionChange
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (orgGroup) => {
          const org = orgGroup?.org;
          if (!org) {
            this.selectedOrganization = undefined;
            return;
          }
          let updatedOrganization = this.organizations.find(value => value.organizationId === org?.organizationId);
          if (updatedOrganization?.name && org.name != updatedOrganization?.name) {
            updatedOrganization.name = org.name!;
          }
          this.selectedOrganization = updatedOrganization;
        }
      })
  }


  onSelectOrganization() {
    if (this.selectedOrganization?.isOrgAccessBlocked) {
      return;
    }
    if (this.selectedOrganization) {
      this.organizationService.updateOrganizationSelection({
        org: this.selectedOrganization
      });
    }
  }

  openCreateOrganizationDialog() {
    const ref = this.dialogService.open(OrgCreateDialogComponent, {
      width: '45vw',
      breakpoints: {
        '1199px': '75vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: 'Create new organization'
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: () => {
          this.authService.updateCurrentUser();
        }
      })
  }

}
