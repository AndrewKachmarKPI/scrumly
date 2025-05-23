import { Component, OnDestroy, OnInit } from '@angular/core';
import { EventService } from '../../modules/events/services/event.service';
import { filter, Subject, Subscription, takeUntil } from 'rxjs';
import { ActivityDto } from '../../modules/events/model/activity.model';
import {
  EventPreSelection
} from '../../modules/events/components/schedule-event-sidebar/schedule-event-sidebar.component';
import {
  OrganizationGroupSelection,
  OrganizationService
} from '../../modules/organizations/services/organization.service';

@Component({
  selector: 'member-dashboard-outlet',
  templateUrl: './member-dashboard-outlet.component.html',
  styleUrl: './member-dashboard-outlet.component.css'
})
export class MemberDashboardOutlet implements OnInit, OnDestroy {
  isSideBarOpened: boolean = false;

  scheduleEventState = false;
  editEventState = false;
  activityDto?: ActivityDto;
  preSelectedTemplate?: EventPreSelection;

  selectedGroup?: OrganizationGroupSelection;

  private destroy$ = new Subject<void>();

  constructor(private eventService: EventService,
              private organizationService: OrganizationService) {
    this.organizationService.onOrganizationEventSubject.next(true);
  }


  ngOnInit(): void {
    this.eventService.onScheduleEventSidebarState
      .pipe(takeUntil(this.destroy$))
      .subscribe((state) => {
        this.scheduleEventState = state;
      });
    this.eventService.onEditEventSidebarState
      .pipe(takeUntil(this.destroy$))
      .subscribe((state) => {
        this.activityDto = state;
        this.editEventState = !!state;
      });
    this.organizationService.onOrganizationSelectionChange
      .pipe(takeUntil(this.destroy$))
      .subscribe(group => {
        this.selectedGroup = group;
      })
    this.eventService.onTemplateSelectionState
      .pipe(filter(Boolean), takeUntil(this.destroy$))
      .subscribe((state) => {
        if (state && !state?.teamId) {
          state.teamId = this.selectedGroup?.team?.teamId!;
        }
        this.preSelectedTemplate = state;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
