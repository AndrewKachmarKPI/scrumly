import { Component, OnDestroy, OnInit } from '@angular/core';
import { BacklogService } from '../../services/backlog.service';
import { filter, Subject, takeUntil } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { BacklogDto } from '../../model/backlog.model';
import { OrganizationGroupSelection, OrganizationService } from '../../../organizations/services/organization.service';
import {
  control,
  defaultPageOptions,
  getInviteSeverity,
  getMemberRoleSeverity
} from '../../../../../ui-components/services/utils';
import { OrganizationMemberRole } from '../../../organizations/model/organization.model';
import {
  ScheduleCalendarEventDialogComponent
} from '../../../events/components/schedule-calendar-event-dialog/schedule-calendar-event-dialog.component';
import { CreateBacklogDialogComponent } from '../../dialogs/create-backlog-dialog/create-backlog-dialog.component';
import { DialogService } from 'primeng/dynamicdialog';

@Component({
  selector: 'app-teams-backlog',
  templateUrl: './team-backlogs.component.html',
  styleUrl: './team-backlogs.component.css'
})
export class TeamBacklogsComponent implements OnInit, OnDestroy {
  teamId?: string;
  backlogs: BacklogDto[] = [];
  selection?: OrganizationGroupSelection;

  selectedBacklogId?: string;

  private destroy$ = new Subject<void>();


  constructor(private activateRoute: ActivatedRoute,
              private backlogService: BacklogService,
              private organizationService: OrganizationService,
              private dialogService: DialogService) {
  }

  ngOnInit(): void {
    this.organizationService.onOrganizationSelectionChange
      .pipe(takeUntil(this.destroy$))
      .subscribe(org => {
        this.selection = org;
      });
    this.backlogService.onBacklogSelection
      .pipe(takeUntil(this.destroy$), filter(Boolean))
      .subscribe(selectedBacklogId => {
        this.selectedBacklogId = selectedBacklogId.backlogId;
      });
    this.activateRoute.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.teamId = params['teamId'];
        this.loadBacklog();
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadBacklog() {
    this.backlogService.hasBacklog(this.teamId!)
      .subscribe({
        next: (hasBacklog) => {
          if (!hasBacklog) {
            this.createBacklog();
          } else {
            this.findBacklogs();
          }
        }
      });
  }

  createBacklog() {
    this.backlogService.createDefaultBacklog(this.teamId!)
      .subscribe({
        next: (backlog) => {
          this.backlogs?.push(backlog);
        }
      })
  }

  findBacklogs() {
    this.backlogService.getTeamBacklogs(this.teamId!)
      .subscribe({
        next: (backlog) => {
          this.backlogs = backlog;
        }
      });
  }

  onSelectBacklog(backlog: BacklogDto) {
    this.backlogService.selectBacklog(backlog)
  }

  onCreateNewBacklog() {
    const ref = this.dialogService.open(CreateBacklogDialogComponent, {
      width: '35vw',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      header: 'Create backlog',
      data: {
        teamId: this.teamId
      }
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: (backlog) => {
          if (backlog) {
            this.backlogService.selectBacklog(backlog);
            this.loadBacklog();
          }
        }
      })
  }

  protected readonly defaultPageOptions = defaultPageOptions;
  protected readonly control = control;
  protected readonly OrganizationMemberRole = OrganizationMemberRole;
  protected readonly getMemberRoleSeverity = getMemberRoleSeverity;
  protected readonly getInviteSeverity = getInviteSeverity;
}
