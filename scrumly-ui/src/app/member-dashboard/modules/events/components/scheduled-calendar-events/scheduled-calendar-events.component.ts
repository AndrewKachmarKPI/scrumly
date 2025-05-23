import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { ActivityService } from "../../services/activity.service";
import {
  CompareOption,
  SearchOperators,
  SearchQuery
} from "../../../../../ui-components/models/search-filter.model";
import { SearchFilterService } from "../../../../../ui-components/services/search-filter.service";
import { ActivityDto } from "../../model/activity.model";
import { EventPreSelection } from "../schedule-event-sidebar/schedule-event-sidebar.component";

@Component({
  selector: 'scheduled-calendar-events',
  templateUrl: './scheduled-calendar-events.component.html',
  styleUrl: './scheduled-calendar-events.component.css',
  providers: [SearchFilterService]
})
export class ScheduledCalendarEventsComponent implements OnInit, OnChanges, OnDestroy {
  @Input() teamId?: string;
  @Input() userId?: string;
  @Input() activities?: ActivityDto[];
  @Output() onFilterChange: EventEmitter<SearchQuery> = new EventEmitter<SearchQuery>();
  @Output() onJoinWorkspace: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onOpenWorkspace: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onEditActivity: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onCancelActivity: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onCancelRecurrentActivity: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onScheduleExternalEvent: EventEmitter<ActivityDto> = new EventEmitter<ActivityDto>();
  @Output() onCreateActivity: EventEmitter<EventPreSelection> = new EventEmitter<EventPreSelection>();

  constructor(private filterService: SearchFilterService) {
  }

  ngOnInit(): void {
    this.filterService.resetFilterService();
    if (this.teamId) {
      this.filterService.applySearchFilter({
        value: this.teamId,
        operator: SearchOperators.EQUALS,
        property: 'scheduledEvent.createdFor',
        compareOption: CompareOption.AND
      });
    }
    if (this.userId) {
      this.filterService.applySearchFilter({
        value: this.userId,
        operator: SearchOperators.IN,
        property: 'scheduledEvent.attendees.userId',
        compareOption: CompareOption.AND
      });
    }
    this.filterService.changePagination({
      pageSize: 1000,
      pageIndex: 0
    });
    console.log("this.filterService.searchQuery:", this.filterService.searchQuery);
    this.onFilterChange.emit(this.filterService.searchQuery);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['teamId'] && !changes['teamId'].isFirstChange()) {
      this.filterService.applySearchFilter({
        value: this.teamId,
        operator: SearchOperators.EQUALS,
        property: 'scheduledEvent.createdFor',
        compareOption: CompareOption.AND
      });
      this.onFilterChange.emit(this.filterService.searchQuery);
    }
    if (changes['userId'] && !changes['userId'].isFirstChange()) {
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


  onDateChange(dates: Date[]) {
    this.filterService.applySearchFilters([
      {
        value: dates[0],
        compareOption: CompareOption.AND,
        property: 'scheduledEvent.startDateTime',
        operator: SearchOperators.GREATER_THAN_OR_EQUAL_TO
      },
      {
        value: dates[1],
        compareOption: CompareOption.AND,
        property: 'scheduledEvent.endDateTime',
        operator: SearchOperators.LESS_THAN_OR_EQUAL_TO
      }
    ]);
    this.onFilterChange.emit(this.filterService.searchQuery);
  }

  onRemoveClick(activity: ActivityDto) {
    if (activity.recurringEventId) {
      this.onCancelRecurrentActivity.emit(activity);
    } else {
      this.onCancelActivity.emit(activity);
    }
  }
}
