import { Component, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TeamService } from '../../../organizations/services/team.service';
import { TeamDto } from '../../../organizations/model/organization.model';
import {
  endOfDay,
  getActivitySeverity,
  getTeamMemberRoleSeverity,
  isBefore,
  startOfDay
} from '../../../../../ui-components/services/utils';
import { ActivityService } from '../../../events/services/activity.service';
import { SearchFilterService } from '../../../../../ui-components/services/search-filter.service';
import { ActivityDto } from '../../../events/model/activity.model';
import { CompareOption, PageDto, SearchOperators } from '../../../../../ui-components/models/search-filter.model';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../../../../auth/auth.service';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-my-team',
  templateUrl: './my-team.component.html',
  styleUrl: './my-team.component.css'
})
export class MyTeamComponent implements OnDestroy {
  teamId?: string;
  userId?: string;
  team?: TeamDto;

  activities?: PageDto<ActivityDto>;

  pendingEvents?: ActivityDto[] = [];
  pastEvents?: ActivityDto[] = [];

  private destroy$ = new Subject<void>();


  constructor(private activateRoute: ActivatedRoute,
              private authService: AuthService,
              private activityService: ActivityService,
              private teamService: TeamService,
              private filterService: SearchFilterService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.userId = this.authService.getUserId();
    this.activateRoute.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.teamId = params['teamId'];
        this.loadTeam();
        this.loadActivities();
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    this.filterService.resetFilterService();
  }


  loadTeam() {
    this.teamService.findTeamById(this.teamId!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (team) => {
          this.team = team;
        }
      })
  }

  loadActivities() {
    const now = new Date();
    this.filterService.applySearchFilter({
      compareOption: CompareOption.AND,
      value: this.teamId,
      operator: SearchOperators.EQUALS,
      property: 'teamId'
    });
    this.filterService.applySearchFilters([
      {
        value: startOfDay(now),
        compareOption: CompareOption.AND,
        property: 'scheduledEvent.startDateTime',
        operator: SearchOperators.GREATER_THAN_OR_EQUAL_TO
      },
      {
        value: endOfDay(now),
        compareOption: CompareOption.AND,
        property: 'scheduledEvent.endDateTime',
        operator: SearchOperators.LESS_THAN_OR_EQUAL_TO
      }
    ]);
    this.filterService.changePagination({
      pageSize: 100,
      pageIndex: 0
    })
    this.activityService.findActivities(this.filterService.searchQuery!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page) => {
          this.activities = page;
          this.pendingEvents = this.activities.data.filter(activity =>
            new Date(activity.scheduledEvent.startDateTime!) >= now
          );
          this.pastEvents = this.activities.data.filter(activity =>
            isBefore(new Date(activity.scheduledEvent.endDateTime!), now)
          );
        }
      });
  }


  protected readonly getTeamMemberRoleSeverity = getTeamMemberRoleSeverity;
  protected readonly getActivitySeverity = getActivitySeverity;
}
