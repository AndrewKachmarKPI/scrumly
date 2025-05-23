import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import {
  addDays,
  control,
  defaultPageOptions,
  endOfDay,
  getActivitySeverity,
  isPast, openLink,
  startOfDay
} from '../../../../../ui-components/services/utils';
import {
  CompareOption,
  PageDto,
  SearchOperators,
  SearchQuery, SortDirection
} from '../../../../../ui-components/models/search-filter.model';
import { ActivityDto, ActivityStatus } from '../../model/activity.model';
import { FormControl, FormGroup } from '@angular/forms';
import { EventService } from '../../services/event.service';
import { SearchFilterService } from '../../../../../ui-components/services/search-filter.service';
import { MenuItem } from 'primeng/api';
import { AuthService } from '../../../../../auth/auth.service';
import { PaginatorState } from 'primeng/paginator';

@Component({
  selector: 'scheduled-events-table',
  templateUrl: './scheduled-events-table.component.html',
  styleUrl: './scheduled-events-table.component.css',
  providers: [ SearchFilterService ]
})
export class ScheduledEventsTableComponent implements OnInit, OnChanges, OnDestroy {
  @Input() teamId?: string;
  @Input() userId?: string;
  @Input() hideFilter: boolean = false;
  @Input() hideActions: boolean = false;
  @Input() showPagination: boolean = true;
  @Input() activities?: PageDto<ActivityDto>;
  @Output() onFilterChange: EventEmitter<SearchQuery> = new EventEmitter<SearchQuery>();
  @Output() onJoinWorkspace: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onCreateAndJoinWorkspace: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onOpenWorkspace: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onEditActivity: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onCancelActivity: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onCancelRecurrentActivity: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onRemoveActivity: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onRemoveRecurrentActivity: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onScheduleExternalEvent: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();

  filterGroup!: FormGroup;
  cols = [
    { field: 'scheduledEvent.title', customExportHeader: 'Activity name' },
    { field: 'scheduledEvent.createdByName', customExportHeader: 'Organizer' },
    { field: 'scheduledEvent.startDateTime', customExportHeader: 'Start date time' },
    { field: 'scheduledEvent.endDateTime', customExportHeader: 'End date time' },
    { field: 'scheduledEvent.duration', customExportHeader: 'Duration' },
    { field: 'status', customExportHeader: 'Status' },
    { field: 'activityTemplate.name', customExportHeader: 'Activity template' },
  ];
  selectedActivity?: ActivityDto;
  joinMenuItems: MenuItem[] = [
    {
      id: 'conference',
      label: 'Join conference',
      icon: 'pi pi-video',
      command: (event) => this.onJoinRoom(this.selectedActivity!)
    },
    {
      id: 'workspace',
      label: 'Open workspace',
      icon: 'pi pi-objects-column',
      command: (event) => this.onOpenRoom(this.selectedActivity!)
    }
  ];

  constructor(private eventService: EventService,
              private filterService: SearchFilterService,
              private authService: AuthService) {
    this.filterGroup = new FormGroup({
      teamId: new FormControl(''),
      datePicker: new FormControl([ startOfDay(new Date()), addDays(endOfDay(new Date()), 7) ]),
      searchField: new FormControl('')
    });
  }

  ngOnInit(): void {
    this.initTableFilters();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['teamId'] && changes['teamId'].currentValue) {
      this.filterService.applySearchFilter({
        value: this.teamId,
        operator: SearchOperators.EQUALS,
        property: 'scheduledEvent.createdFor',
        compareOption: CompareOption.AND
      });
      this.onFilterChange.emit(this.filterService.searchQuery);
    }
    if (changes['userId'] && changes['userId'].currentValue) {
      this.filterService.applySearchFilter({
        value: this.userId,
        operator: SearchOperators.IN,
        property: 'scheduledEvent.attendees.userId',
        compareOption: CompareOption.AND
      });
      this.onFilterChange.emit(this.filterService.searchQuery);
    }
  }


  ngOnDestroy(): void {
    this.filterService.resetFilterService();
  }

  onClearDate() {
    this.filterService.resetSearchFilter('scheduledEvent.startDateTime');
    this.filterService.resetSearchFilter('scheduledEvent.endDateTime');
    this.onFilterChange.emit(this.filterService.searchQuery);
  }

  onSelectDate(date: Date[]) {
    if (date.filter(Boolean).length != 2) {
      return;
    }
    this.filterService.applySearchFilters([
      {
        value: startOfDay(date[0]),
        compareOption: CompareOption.AND,
        property: 'scheduledEvent.startDateTime',
        operator: SearchOperators.GREATER_THAN_OR_EQUAL_TO
      },
      {
        value: endOfDay(date[1]),
        compareOption: CompareOption.AND,
        property: 'scheduledEvent.endDateTime',
        operator: SearchOperators.LESS_THAN_OR_EQUAL_TO
      }
    ]);
    this.onFilterChange.emit(this.filterService.searchQuery);
  }

  onPageChange(page: PaginatorState) {
    this.filterService.changePagination({
      pageSize: page.rows || this.activities?.size!,
      pageIndex: page.page! || 0
    });
    this.onFilterChange.emit(this.filterService.searchQuery);
  }

  onSearchChange() {
    this.filterService.applySearchFilter({
      value: this.control(this.filterGroup, 'searchField').value,
      property: 'scheduledEvent.title',
      compareOption: CompareOption.AND,
      operator: SearchOperators.LIKE
    });
    this.onFilterChange.emit(this.filterService.searchQuery);
  }

  onJoinRoom(activity: ActivityDto) {
    if (activity.scheduledEvent.active) {
      this.onJoinWorkspace.emit(activity);
    }
  }

  onCreateAndJoinRoom(activity: ActivityDto) {
    if (activity.scheduledEvent.active) {
      this.onCreateAndJoinWorkspace.emit(activity);
    }
  }


  onOpenRoom(activity: ActivityDto) {
    this.onOpenWorkspace.emit(activity);
  }

  onSelectActivity(activity: ActivityDto) {
    this.selectedActivity = activity;
    this.joinMenuItems.forEach(item => {
      if (item.id === 'conference') {
        const hasConference = !this.selectedActivity?.workspace?.conferenceId!;
        if (hasConference) {
          item.label = 'Create and Join Conference'
          item.command = (event) => this.onCreateAndJoinRoom(this.selectedActivity!);
        }
      }
      if (item.id === 'workspace') {
        item.disabled = !this.selectedActivity?.workspace?.workspaceId!;
      }
    })
  }

  isCurrentUser(userId: string) {
    return this.authService.isCurrentUser(userId);
  }


  toggleScheduleSidebar() {
    this.eventService.scheduleEventSidebarState.next(true);
  }


  getMenuItems(activity: ActivityDto): MenuItem[] {
    let items: MenuItem[] = [];
    if (activity.status === ActivityStatus.SCHEDULED ||
      activity.status === ActivityStatus.CREATED) {
      let editItem: MenuItem = {
        label: 'Edit options',
        items: [
          {
            label: 'Change event',
            icon: 'pi pi-pencil',
            command: () => this.onEditActivity.emit(activity)
          },
          {
            label: 'Schedule External Event',
            icon: 'pi pi-calendar',
            command: () => this.onScheduleExternalEvent.emit(activity)
          }
        ]
      }
      items.push(editItem);

      if (activity.scheduledEvent.active) {
        let cancelItem: MenuItem = {
          label: 'Cancel Options',
          items: [
            {
              label: 'Cancel this event',
              icon: 'pi pi-times',
              command: () => this.onCancelActivity.emit(activity)
            }
          ]
        }
        if (activity.recurringEventId) {
          cancelItem.items?.push({
            label: 'Cancel all recurrent events',
            icon: 'pi pi-trash',
            command: () => this.onCancelRecurrentActivity.emit(activity)
          })
        }
        items.push(cancelItem);
      }
    }
    return items;
  }

  private initTableFilters() {
    this.filterService.changeTableSort({
      sortKey: 'scheduledEvent.active',
      sortDirection: SortDirection.DESCENDING
    });
    this.filterService.changeTableSort({
      sortKey: 'scheduledEvent.startDateTime',
      sortDirection: SortDirection.ASCENDING
    });
    this.filterService.applySearchFilter({
      operator: SearchOperators.IS_NOT_NULL,
      compareOption: CompareOption.AND,
      property: 'scheduledEvent',
      value: ''
    });
    this.filterService.applySearchFilters([
      {
        value: startOfDay(new Date()),
        compareOption: CompareOption.AND,
        property: 'scheduledEvent.startDateTime',
        operator: SearchOperators.GREATER_THAN_OR_EQUAL_TO
      },
      {
        value: addDays(endOfDay(new Date()), 7),
        compareOption: CompareOption.AND,
        property: 'scheduledEvent.endDateTime',
        operator: SearchOperators.LESS_THAN_OR_EQUAL_TO
      }
    ]);

    if (this.userId) {
      this.filterService.applySearchFilter({
        value: this.userId,
        operator: SearchOperators.IN,
        property: 'scheduledEvent.attendees.userId',
        compareOption: CompareOption.AND
      });
    }

    if (this.teamId) {
      this.filterService.applySearchFilters([
        {
          value: this.teamId,
          operator: SearchOperators.EQUALS,
          property: 'scheduledEvent.createdFor',
          compareOption: CompareOption.AND
        }
      ]);
    }
    this.onFilterChange.emit(this.filterService.searchQuery);
  }

  protected readonly getActivitySeverity = getActivitySeverity;
  protected readonly isPast = isPast;
  protected readonly control = control;
  protected readonly defaultPageOptions = defaultPageOptions;
  protected readonly openLink = openLink;
}
