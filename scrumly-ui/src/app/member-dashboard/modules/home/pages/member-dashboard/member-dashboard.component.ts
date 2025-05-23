import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivityService } from '../../../events/services/activity.service';
import { ActivityDto } from '../../../events/model/activity.model';
import { AuthService } from '../../../../../auth/auth.service';
import { CompareOption, SearchOperators } from '../../../../../ui-components/models/search-filter.model';
import { endOfDay, startOfDay } from '../../../../../ui-components/services/utils';
import { filter, Subject, takeUntil } from 'rxjs';
import { SearchFilterService } from '../../../../../ui-components/services/search-filter.service';
import { ActivityStatisticService } from '../../../events/services/activity-statistic.service';
import { ActivityUserStatistic, WeeklyLoadRecord } from '../../../events/model/activity-statistic.model';
import { TranslateService } from '@ngx-translate/core';
import { ActivityListDialogComponent } from '../../components/activity-list-dialog/activity-list-dialog.component';
import { DialogService } from 'primeng/dynamicdialog';
import { format } from 'date-fns-tz';
import { Router } from '@angular/router';

@Component({
  selector: 'app-member-dashboard',
  templateUrl: './member-dashboard.component.html',
  styleUrl: './member-dashboard.component.css'
})
export class MemberDashboardComponent implements OnInit, OnDestroy {
  userId?: string;
  activities: ActivityDto[] = [];
  statistic?: ActivityUserStatistic;

  private destroy$ = new Subject<void>();

  statusPieChartData: any;
  statusPieChartOptions: any;

  weeklyLoadLineChartData: any;
  weeklyLoadLineChartOptions: any;

  selectedDates: Date[] = [];

  constructor(private authService: AuthService,
              private activityStatisticService: ActivityStatisticService,
              private activityService: ActivityService,
              private filterService: SearchFilterService,
              private dialogService: DialogService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.authService.onProfileUpdate
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (userProfile) => {
          this.userId = userProfile?.userId;
          this.initDashboard();
        }
      })
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    this.filterService.resetFilterService();
  }

  private initDashboard() {
    this.loadActivities();
    this.loadStatistic();
  }

  onWeekUpdate(dates: Date[]) {
    this.selectedDates = dates;
    this.loadStatistic();
  }

  loadStatistic() {
    const stringDates: string[] = this.selectedDates.map(date => format(date, 'yyyy-MM-dd'));
    this.activityStatisticService.getMyActivityUserStatistic(stringDates)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (statistic) => {
          this.statistic = statistic;
          this.initCharts();
        }
      });
  }

  initCharts() {
    this.initStatusPieChart();
    this.initWeeklyLineChart();
  }

  private initStatusPieChart() {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');

    this.statusPieChartData = {
      labels: this.statistic?.eventStatusBreakdown.statusRecords
        .map(record => record.status),
      datasets: [
        {
          data: this.statistic?.eventStatusBreakdown.statusRecords
            .map(record => record.total),
          backgroundColor: [
            documentStyle.getPropertyValue('--green-500'),
            documentStyle.getPropertyValue('--yellow-500'),
            documentStyle.getPropertyValue('--blue-500'),
          ],
          hoverBackgroundColor: [
            documentStyle.getPropertyValue('--green-400'),
            documentStyle.getPropertyValue('--yellow-400'),
            documentStyle.getPropertyValue('--blue-400'),
          ]
        }
      ]
    };
    this.statusPieChartOptions = {
      plugins: {
        legend: {
          labels: {
            usePointStyle: true,
            color: textColor
          }
        }
      }
    };
  }

  private initWeeklyLineChart() {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    this.weeklyLoadLineChartData = {
      labels: this.statistic?.weeklyMeetingLoadStats?.weeklyLoadRecords
        .map(record => record.day),
      datasets: [
        {
          label: 'Week load',
          backgroundColor: documentStyle.getPropertyValue('--green-500'),
          borderColor: documentStyle.getPropertyValue('--green-500'),
          data: this.statistic?.weeklyMeetingLoadStats?.weeklyLoadRecords
            .map(record => record.total),
        }
      ]
    };

    this.weeklyLoadLineChartOptions = {
      maintainAspectRatio: false,
      aspectRatio: 0.8,
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        }
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary,
            font: {
              weight: 500
            }
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        },
        y: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          }
        }

      }
    };
  }

  onWeeklyLoadDataSelect(event: any) {
    const index = event.element.index;
    const data: WeeklyLoadRecord | undefined = this.statistic?.weeklyMeetingLoadStats.weeklyLoadRecords[index]!;
    if (data) {
      const ref = this.dialogService.open(ActivityListDialogComponent, {
        width: 'auto',
        breakpoints: {
          '1199px': '75vw',
          '575px': '90vw'
        },
        resizable: true,
        draggable: false,
        closeOnEscape: true,
        closable: true,
        header: `Events on ${ data.day }`,
        data: {
          activities: data.activities
        }
      });
    }
  }

  loadActivities() {
    const now = new Date();
    this.filterService.applySearchFilter({
      compareOption: CompareOption.AND,
      value: this.userId,
      operator: SearchOperators.EQUALS,
      property: 'scheduledEvent.attendees.userId'
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
          this.activities = page.data;
        }
      });
  }

  onScheduleEvent() {
    this.router.navigate([ '/app/events/meetings' ], {
      queryParams: {
        schedule: true
      }
    });
  }
}
