import { Component, OnDestroy, OnInit } from '@angular/core';
import { control, defaultPageOptions, isPast } from '../../../../../ui-components/services/utils';
import { SearchFilterService } from '../../../../../ui-components/services/search-filter.service';
import { PageDto, SearchQuery } from '../../../../../ui-components/models/search-filter.model';
import { ActivatedRoute, Router } from '@angular/router';
import { ActivityService } from '../../services/activity.service';
import { ActivityDto, ActivityStatus } from '../../model/activity.model';
import { OrganizationMemberRole, TeamDto } from '../../../organizations/model/organization.model';
import { filter } from 'rxjs';
import { TeamService } from '../../../organizations/services/team.service';
import { AuthService } from '../../../../../auth/auth.service';
import { UserProfileDto } from '../../../../../auth/auth.model';
import { EventService } from '../../services/event.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import {
  ScheduleCalendarEventDialogComponent
} from '../../components/schedule-calendar-event-dialog/schedule-calendar-event-dialog.component';
import { DialogService } from 'primeng/dynamicdialog';
import { EventPreSelection } from '../../components/schedule-event-sidebar/schedule-event-sidebar.component';
import { WorkspaceService } from '../../../../../meeting-workspace/services/workspace.service';

@Component({
  selector: 'scheduled-events-page',
  templateUrl: './scheduled-events-page.component.html',
  styleUrl: './scheduled-events-page.component.css'
})
export class ScheduledEventsPageComponent implements OnInit, OnDestroy {
  teamId?: string;
  userId?: string;
  searchQuery?: SearchQuery;

  activities?: PageDto<ActivityDto>;
  team?: TeamDto;
  profile?: UserProfileDto;

  activityViewMode: 'table' | 'calendar' = 'table';
  activityViewModes = [
    {
      label: 'Table view',
      icon: 'pi pi-list',
      value: 'table'
    },
    {
      label: 'Calendar view',
      icon: 'pi pi-calendar',
      value: 'calendar'
    }
  ]

  constructor(private activityService: ActivityService,
              private teamService: TeamService,
              private eventService: EventService,
              public filterService: SearchFilterService,
              private activateRoute: ActivatedRoute,
              private router: Router,
              private workspaceService: WorkspaceService,
              private confirmationService: ConfirmationService,
              private messageService: MessageService,
              private dialogService: DialogService,
              private authService: AuthService) {
  }


  ngOnInit(): void {
    this.activateRoute.queryParams
      .pipe(filter(param => param['mode'] || param['schedule']))
      .subscribe(params => {
        if (params['mode']) {
          this.activityViewMode = params['mode'];
        }
        if (params['schedule']) {
          this.eventService.scheduleEventSidebarState.next(true);
        }
      });
    this.authService.onProfileUpdate.subscribe({
      next: (profile) => {
        if (profile) {
          this.profile = profile;
          this.userId = profile.userId;
        }
      }
    });
    this.activateRoute.params.subscribe(params => {
      this.teamId = params['teamId'];
      if (this.teamId) {
        this.loadTeam();
      }
    });
    this.activityService.onEventCreated
      .pipe(filter(Boolean))
      .subscribe(() => {
        this.loadActivities(this.searchQuery);
      });
  }

  ngOnDestroy(): void {
    this.filterService.resetFilterService();
  }

  loadActivities(searchQuery?: SearchQuery) {
    this.activityService.findActivities(searchQuery!).subscribe({
      next: (page) => {
        this.activities = page;
      }
    });
  }

  loadTeam() {
    this.teamService.findTeamById(this.teamId!).subscribe({
      next: (team) => {
        this.team = team;
      }
    })
  }

  onFilterChange(searchQuery: SearchQuery) {
    this.searchQuery = searchQuery;
    this.loadActivities(searchQuery);
  }

  joinWorkspace(activity: ActivityDto): void {
    if (activity.status === ActivityStatus.FINISHED) {
      this.openWorkspace(activity);
      return;
    }
    if (activity.workspace.conferenceId) {
      this.joinConference(activity, activity.workspace.conferenceId!);
    } else if (activity.workspace.workspaceId) {
      this.openWorkspace(activity);
    }
  }

  private joinConference(activity: ActivityDto, conferenceId: string) {
    this.router.navigate([ `/app/workspace/conference/${ conferenceId }` ], {
      queryParams: {
        activityId: activity.activityId,
        workspaceId: activity.workspace.workspaceId
      }
    })
  }

  createAndJoinWorkspace(activity: ActivityDto): void {
    if (activity.workspace.conferenceId) {
      this.joinWorkspace(activity);
    } else {
      this.workspaceService.createConference(activity.workspace.workspaceId!)
        .subscribe({
          next: (workspace) => {
            if (workspace.conferenceId!) {
              this.joinConference(activity, workspace.conferenceId!);
            }
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: 'Join error',
              detail: 'Failed to create and join conference'
            });
          }
        })
    }
  }


  openWorkspace(activity: ActivityDto) {
    if (!activity.workspace.workspaceId) {
      return;
    }
    this.router.navigate([ `/app/workspace/dashboard` ], {
      queryParams: {
        activityId: activity.activityId,
        workspaceId: activity.workspace.workspaceId
      }
    })
  }

  editActivity(activity: ActivityDto): void {
    if (isPast(activity.scheduledEvent.endDateTime!)) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Past event',
        detail: 'You cannot modify past events'
      });
      return;
    }
    this.eventService.editEventSidebarState.next(activity);
  }

  cancelActivity(activity: ActivityDto): void {
    const confirmConfig = !activity.recurringEventId
      ? {
        message: 'Are you sure that you want to cancel this activity?',
        header: 'Cancel activity',
        icon: 'pi pi-exclamation-triangle',
        acceptIcon: 'none',
        rejectIcon: 'none',
        acceptButtonStyleClass: 'p-button-success',
        rejectButtonStyleClass: 'p-button-text',
        accept: () => {
          this.sendCancelActivity(activity);
        }
      }
      : {
        message: 'Are you sure that you want to cancel all recurrent activities?',
        header: 'Cancel All activities',
        icon: 'pi pi-exclamation-triangle',
        acceptIcon: 'none',
        rejectIcon: 'none',
        acceptButtonStyleClass: 'p-button-success',
        rejectButtonStyleClass: 'p-button-text',
        accept: () => {
          this.sendCancelRecurrentActivity(activity);
        }
      }
    this.confirmationService.confirm(confirmConfig);
  }

  scheduleExternalEvent(activity: ActivityDto) {
    const ref = this.dialogService.open(ScheduleCalendarEventDialogComponent, {
      width: '35vw',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: 'Schedule calendar event',
      data: {
        activity: activity
      }
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: () => {
          this.onFilterChange(this.searchQuery!);
        }
      })
  }

  openCreateActivity(selection: EventPreSelection) {
    if (!selection) {
      return;
    }
    selection.teamId = this.teamId;
    this.eventService.scheduleEventSidebarState.next(true);
    this.eventService.templateSelectionState.next(selection)
  }


  private sendCancelActivity(activity: ActivityDto) {
    this.activityService.cancelActivity(activity.activityId).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Activity canceled',
          detail: 'Your have successfully canceled this activity',
        });
        this.onFilterChange(this.searchQuery!);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to cancel activity',
          detail: err.error.message,
        });
      },
    });
  }

  private sendCancelRecurrentActivity(activity: ActivityDto) {
    this.activityService.cancelAllRecurrentActivity(activity.recurringEventId).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Activity canceled',
          detail: 'Your have successfully canceled all recurrent activities',
        });
        this.onFilterChange(this.searchQuery!);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to cancel activity',
          detail: err.error.message,
        });
      },
    });
  }

  protected readonly control = control;
  protected readonly OrganizationMemberRole = OrganizationMemberRole;
  protected readonly defaultPageOptions = defaultPageOptions;
}
