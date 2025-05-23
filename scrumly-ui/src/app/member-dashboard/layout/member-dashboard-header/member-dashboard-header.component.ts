import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { MenuItem } from 'primeng/api';
import {
  OrganizationGroupSelection,
  OrganizationService
} from '../../modules/organizations/services/organization.service';
import { control } from '../../../ui-components/services/utils';
import { FormControl, FormGroup } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'member-dashboard-header',
  templateUrl: './member-dashboard-header.component.html',
  styleUrl: './member-dashboard-header.component.css'
})
export class MemberDashboardHeaderComponent implements OnInit, OnDestroy {
  items: MenuItem[] | undefined;
  @Output() toggleSidebar: EventEmitter<boolean> = new EventEmitter();
  public searchGroup!: FormGroup;

  selectedGroup?: OrganizationGroupSelection;
  private destroy$ = new Subject<void>();


  constructor(private organizationService: OrganizationService) {
    this.searchGroup = new FormGroup({
      teamId: new FormControl('')
    });
  }

  ngOnInit(): void {
    this.items = [];
    this.organizationService.onOrganizationSelectionChange
      .pipe(takeUntil(this.destroy$))
      .subscribe(group => {
        this.selectedGroup = group;
        if (this.selectedGroup?.team) {
          control(this.searchGroup, 'teamId').setValue(this.selectedGroup.team.teamId);
        }
      })
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }


  get organizationId() {
    return this.organizationService.getCurrentOrganizationId()
  }

  onSelectOrganizationGroup(group: OrganizationGroupSelection) {
    const selectionJson = JSON.stringify(group);
    localStorage.setItem('organizationGroupSelection', selectionJson);

    this.organizationService.updateOrganizationSelection(group);
  }

  protected readonly control = control;
}
