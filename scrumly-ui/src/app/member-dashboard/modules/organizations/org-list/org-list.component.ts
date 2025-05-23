import { Component, OnDestroy, OnInit } from '@angular/core';
import { SearchFilterService } from '../../../../ui-components/services/search-filter.service';
import { OrganizationService } from '../services/organization.service';
import { CompareOption, PageDto, SearchOperators } from '../../../../ui-components/models/search-filter.model';
import { OrganizationDto, OrganizationInfoDto, OrganizationStatus } from '../model/organization.model';
import { DialogService } from 'primeng/dynamicdialog';
import {
  OrgCreateDialogComponent
} from '../dialogs/org-create-dialog/org-create-dialog.component';
import { filter, Subject, takeUntil } from 'rxjs';
import { ConfirmationService } from 'primeng/api';
import { RadioButton, RadioButtonClickEvent } from 'primeng/radiobutton';
import { control, defaultPageOptions, joinBy } from '../../../../ui-components/services/utils';
import { PaginatorState } from 'primeng/paginator';
import { FormControl, FormGroup } from '@angular/forms';
import { AuthService } from '../../../../auth/auth.service';

@Component({
  selector: 'app-org-list',
  templateUrl: './org-list.component.html',
  styleUrl: './org-list.component.css'
})
export class OrgListComponent implements OnInit, OnDestroy {
  public selectedOrganization: OrganizationInfoDto | OrganizationDto | null = null;
  public organizations?: PageDto<OrganizationInfoDto>;
  public filtersGroup: FormGroup;

  private destroy$: Subject<void> = new Subject<void>();

  constructor(private searchFilterService: SearchFilterService,
              public organizationService: OrganizationService,
              private confirmationService: ConfirmationService,
              private authService: AuthService,
              private dialogService: DialogService) {

    this.filtersGroup = new FormGroup({
      organizationName: new FormControl(''),
      isActive: new FormControl(null)
    })
  }

  ngOnInit(): void {
    this.loadOrganizations();
    this.organizationService.onOrganizationSelectionChange
      .pipe(takeUntil(this.destroy$))
      .subscribe(org => {
        this.selectedOrganization = org?.org!;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.searchFilterService.resetFilterService();
  }

  onSelectOrganization(radioButton: RadioButton, org: OrganizationInfoDto) {
    if (!org.isActive || org.isOrgAccessBlocked) {
      return;
    }

    if (this.selectedOrganization && this.selectedOrganization?.organizationId == org.organizationId) {
      this.confirmationService.confirm({
        message: 'Are you sure that you want to exit organization?',
        header: 'Exit organization',
        icon: 'pi pi-exclamation-triangle',
        acceptIcon: 'none',
        rejectIcon: 'none',
        acceptButtonStyleClass: 'p-button-success',
        rejectButtonStyleClass: 'p-button-text',
        accept: () => {
          radioButton.checked = false;
        }
      });
      return;
    }
    radioButton.checked = false;
    if (this.selectedOrganization && this.selectedOrganization?.organizationId != org.organizationId) {
      this.confirmationService.confirm({
        message: 'Are you sure that you want to switch organization?',
        header: 'Switch organization',
        icon: 'pi pi-exclamation-triangle',
        acceptIcon: 'none',
        rejectIcon: 'none',
        acceptButtonStyleClass: 'p-button-success',
        rejectButtonStyleClass: 'p-button-text',
        accept: () => {
          radioButton.checked = true;
          this.organizationService.updateOrganizationSelection({
            org: org
          });
        }
      });
    } else {
      radioButton.checked = true;
      this.organizationService.updateOrganizationSelection({
        org: org
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
          this.loadOrganizations();
          this.authService.updateCurrentUser();
          this.organizationService.onOrganizationEventSubject.next(true);
        }
      })
  }

  onPageChange(page: PaginatorState) {
    this.searchFilterService.changePagination({
      pageSize: page.rows!,
      pageIndex: page.page!
    });
    this.loadOrganizations();
  }

  filterByName() {
    this.searchFilterService.applySearchFilter({
      operator: SearchOperators.LIKE,
      value: control(this.filtersGroup, 'organizationName').value,
      compareOption: CompareOption.AND,
      property: 'name'
    });
    this.loadOrganizations();
  }

  loadOrganizations() {
    this.organizationService.findMyOrganizations(this.searchFilterService.searchQuery)
      .subscribe({
        next: (organizations) => {
          this.organizations = organizations;
        }
      });
  }

  protected readonly joinBy = joinBy;
  protected readonly defaultPageOptions = defaultPageOptions;
  protected readonly control = control;
}
