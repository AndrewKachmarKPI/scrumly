import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { EventService } from '../../services/event.service';
import { filter, map } from 'rxjs';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import {
  calendarProviders,
  conferenceProviders,
  control, currentDate, isSameDay,
  startBeforeEndValidator, translateRRule
} from '../../../../../ui-components/services/utils';
import { MessageService } from 'primeng/api';
import moment from 'moment-timezone';
import { CreateActivityRQ, EventAttendeeDto } from '../../model/event.model';
import { TeamDto, TeamMemberDto } from '../../../organizations/model/organization.model';
import { Router } from '@angular/router';
import { ActivityDto, ActivityTemplateDto, TimeSlotDto } from '../../model/activity.model';
import { OrganizationGroupSelection } from '../../../organizations/services/organization.service';
import { DialogService } from 'primeng/dynamicdialog';
import { SelectRecurenceDialogComponent } from '../select-recurence-dialog/select-recurence-dialog.component';
import { ActivityService } from '../../services/activity.service';
import { AuthService } from '../../../../../auth/auth.service';
import { SpinnerService } from '../../../../../ui-components/services/spinner.service';
import { SelectTimeSlotDialogComponent } from '../select-time-slot-dialog/select-time-slot-dialog.component';


export interface EventPreSelection {
  templateId?: string,
  teamId?: string,
  startDate?: string;
  endDate?: string;
}

@Component({
  selector: 'schedule-event-sidebar',
  templateUrl: './schedule-event-sidebar.component.html',
  styleUrl: './schedule-event-sidebar.component.css'
})
export class ScheduleEventSidebarComponent implements OnInit, OnDestroy, OnChanges {
  @Input() id: string = 'sidebar';
  @Input() editMode = false;
  @Input() sidebarState = false;
  @Input() activityDto?: ActivityDto
  @Input() eventPreSelection?: EventPreSelection;
  @Output() sidebarStateChange = new EventEmitter<boolean>();

  minDate = currentDate();
  eventFormGroup: FormGroup;

  selectedTeam?: TeamDto;
  selectedTemplate?: ActivityTemplateDto;

  selectedMembers: string[] = [];
  excludeUsersList: string[] = [];

  constructor(private eventService: EventService,
              private activityService: ActivityService,
              private messageService: MessageService,
              private dialogService: DialogService,
              private authService: AuthService,
              private spinnerService: SpinnerService,
              private router: Router) {
    this.eventFormGroup = this.initFormGroup();
  }

  ngOnInit(): void {
    this.authService.onProfileUpdate.subscribe({
      next: (user) => {
        this.excludeUsersList = [ user?.userId! ];
      }
    })
  }

  ngOnDestroy(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activityDto'] && changes['activityDto'].currentValue) {
      this.eventFormGroup = this.initFormGroup(this.activityDto);
      if (this.editMode) {
        this.control(this.eventFormGroup, 'createdFor').disable();
        this.control(this.eventFormGroup, 'recurrence').disable();
        this.control(this.eventFormGroup, 'recurrenceText').disable();
      }
    }

    if (changes['eventPreSelection'] && changes['eventPreSelection'].currentValue) {
      if (!this.editMode) {
        const date = this.eventPreSelection?.startDate
          ? new Date(this.eventPreSelection?.startDate)
          : new Date();
        this.control(this.eventFormGroup, 'createdFor').setValue(this.eventPreSelection?.teamId);
        this.control(this.eventFormGroup, 'templateId').setValue(this.eventPreSelection?.templateId);
        this.control(this.eventFormGroup, 'startDate').setValue(date);
        this.control(this.eventFormGroup, 'startTime').setValue(this.eventPreSelection?.startDate);
        this.control(this.eventFormGroup, 'endTime').setValue(this.eventPreSelection?.endDate);
      }
    }
  }

  initFormGroup(activity?: ActivityDto) {
    let attendees: string[] = [];
    let conferenceProvider = undefined;
    let calendarProvider = undefined;
    if (activity) {
      conferenceProvider = conferenceProviders
        .find(value => value.type.name ===
          activity.scheduledEvent.eventMetadata?.conferenceProvider)?.type;
      calendarProvider = calendarProviders
        .find(value => value.type.name ===
          activity.scheduledEvent.eventMetadata?.calendarProvider)?.type;
      this.selectedMembers = activity.scheduledEvent.attendees.map(value => value.userId);
    }


    return new FormGroup({
      createdFor: new FormControl(activity?.scheduledEvent.createdFor || '', Validators.compose([
        Validators.required
      ])),
      templateId: new FormControl(activity?.activityTemplate?.templateId || '', Validators.compose([])),
      title: new FormControl(activity?.scheduledEvent.title || '', Validators.compose([
        Validators.required
      ])),
      recurrenceText: new FormControl(
        activity?.scheduledEvent.recurrence
          ? translateRRule(activity?.scheduledEvent.recurrence.recurrence)
          : ''),
      recurrence: new FormControl(activity?.scheduledEvent.recurrence
        ? activity?.scheduledEvent.recurrence?.recurrence
        : ''),
      location: new FormControl(activity?.scheduledEvent.location || ''),
      description: new FormControl(activity?.scheduledEvent.description || ''),
      startDate: new FormControl(
        activity?.scheduledEvent.startDateTime
          ? new Date(activity?.scheduledEvent.startDateTime!)
          : '', Validators.compose([
          Validators.required
        ])),
      startTime: new FormControl(
        activity?.scheduledEvent.startDateTime
          ? new Date(activity?.scheduledEvent.startDateTime!)
          : '', Validators.compose([
          Validators.required
        ])),
      endTime: new FormControl(activity?.scheduledEvent.endDateTime
        ? new Date(activity?.scheduledEvent.endDateTime!)
        : '', Validators.compose([
        Validators.required
      ])),
      attendees: new FormControl(attendees || [], Validators.compose([
        Validators.required
      ])),
      conferenceProvider: new FormControl(conferenceProvider || ''),
      calendarProvider: new FormControl(calendarProvider || ''),
      modifyRecurrentEvents: new FormControl(false)
    }, { validators: startBeforeEndValidator() });
  }


  toggleSidebarState(): void {
    if (this.editMode) {
      this.eventService.editEventSidebarState.next(undefined);
    } else {
      this.eventService.scheduleEventSidebarState.next(!this.eventService.scheduleEventSidebarState.value);
    }
  }


  onSubmitForm() {
    if (!this.eventFormGroup?.dirty) {
      this.messageService.add({
        severity: 'warn',
        summary: 'No changes found',
        detail: 'Modify your activity',
      });
      return;
    }

    if (this.eventFormGroup?.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Invalid form',
        detail: 'Check form validity',
      });
      this.eventFormGroup.markAllAsTouched();
      return;
    }

    let isRescheduleAll = false;
    if (this.editMode) {
      const initDate = this.activityDto?.scheduledEvent.startDateTime!;
      const newDate = new Date(this.control(this.eventFormGroup, 'startDate').value);
      isRescheduleAll = this.control(this.eventFormGroup, 'modifyRecurrentEvents').value && isSameDay(initDate, newDate);
    }

    const startDate: Date = this.control(this.eventFormGroup, 'startDate').value;
    const startTime: Date = this.control(this.eventFormGroup, 'startTime').value;
    const endTime: Date = this.control(this.eventFormGroup, 'endTime').value;
    const dateZone = moment(startDate).tz(moment.tz.guess()).tz();

    const startDateTime = moment(startDate).set({
      hour: startTime.getHours(),
      minute: startTime.getMinutes(),
      second: startTime.getSeconds(),
      millisecond: startTime.getMilliseconds(),
    }).toDate();
    const endDateTime = moment(startDate).set({
      hour: endTime.getHours(),
      minute: endTime.getMinutes(),
      second: endTime.getSeconds(),
      millisecond: endTime.getMilliseconds(),
    }).toDate();

    const profiles: TeamMemberDto[] = this.control(this.eventFormGroup, 'attendees').value;
    const attendees: EventAttendeeDto[] = profiles.map(profile => {
      return {
        userId: profile.profile?.userId!,
        displayName: profile.profile?.firstName + ' ' + profile.profile?.lastName,
        userEmailAddress: profile.profile?.email!
      }
    })
    const conferenceProvider = this.control(this.eventFormGroup, 'conferenceProvider').value;
    const calendarProvider = this.control(this.eventFormGroup, 'calendarProvider').value;
    const isCreateConference = !!conferenceProvider;
    const isCreateCalendarEvent = !!calendarProvider;

    const recurrenceText = this.control(this.eventFormGroup, 'recurrenceText').value;
    const recurrence = this.control(this.eventFormGroup, 'recurrence').value;

    const rq: CreateActivityRQ = {
      eventDto: {
        title: this.control(this.eventFormGroup, 'title').value,
        location: this.control(this.eventFormGroup, 'location').value,
        description: this.control(this.eventFormGroup, 'description').value,
        createdFor: this.control(this.eventFormGroup, 'createdFor').value,
        startDateTime: startDateTime,
        endDateTime: endDateTime,
        startTimeZone: dateZone,
        endTimeZone: dateZone,
        attendees: attendees,
        createConference: isCreateConference,
      },
      createCalendarEvent: isCreateCalendarEvent,
      createConference: isCreateConference,
      templateId: this.control(this.eventFormGroup, 'templateId').value,
      conferenceProvider: conferenceProvider?.name || '',
      calendarProvider: calendarProvider?.name || '',
    }

    if (recurrence && recurrence != '' && recurrenceText && recurrenceText != '') {
      rq.eventDto.recurrence = {
        recurrenceText: recurrenceText,
        recurrence: recurrence
      }
    }

    if (!this.editMode) {
      this.onScheduleActivity(rq)
    } else if (!isRescheduleAll && this.activityDto?.activityId) {
      this.onReScheduleActivity(this.activityDto?.activityId!, rq);
    } else if (isRescheduleAll && this.activityDto?.activityId) {
      this.onReScheduleRecurrentActivity(this.activityDto?.activityId, rq);
    }
  }

  onScheduleActivity(rq: CreateActivityRQ) {
    this.spinnerService.show();
    this.activityService.scheduleActivity(rq).subscribe({
      next: (event) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Activity scheduled',
          detail: 'Your activity has been successfully scheduled',
        });
        this.router.navigate([], {
          queryParamsHandling: 'merge',
          onSameUrlNavigation: 'reload'
        });
        this.eventFormGroup = this.initFormGroup();
        this.activityService.onEventCreatedSource.next(true);
        this.toggleSidebarState();
      },
      error: (err) => {
        this.spinnerService.hide();
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to schedule meeting event',
          detail: err.error.message,
        });
      },
      complete: () => {
        this.spinnerService.hide();
      }
    })
  }

  onReScheduleActivity(activityId: string, rq: CreateActivityRQ) {
    this.spinnerService.show();
    this.activityService.reScheduleActivity(activityId, rq).subscribe({
      next: (event) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Activity rescheduled',
          detail: 'Your activity has been rescheduled scheduled',
        });
        this.router.navigate([], {
          queryParamsHandling: 'merge',
          onSameUrlNavigation: 'reload'
        });
        this.eventFormGroup = this.initFormGroup();
        this.activityService.onEventCreatedSource.next(true);
        this.toggleSidebarState();
      },
      error: (err) => {
        this.spinnerService.hide();
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to rescheduled meeting event',
          detail: err.error.message,
        });
      },
      complete: () => {
        this.spinnerService.hide();
      }
    })
  }

  onReScheduleRecurrentActivity(activityId: string, rq: CreateActivityRQ) {
    this.spinnerService.show();
    this.activityService.reScheduleRecurrentActivity(activityId, rq).subscribe({
      next: (event) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Activity rescheduled',
          detail: 'Your activity has been rescheduled scheduled',
        });
        this.router.navigate([], {
          queryParamsHandling: 'merge',
          onSameUrlNavigation: 'reload'
        });
        this.eventFormGroup = this.initFormGroup();
        this.activityService.onEventCreatedSource.next(true);
        this.toggleSidebarState();
      },
      error: (err) => {
        this.spinnerService.hide();
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to rescheduled meeting event',
          detail: err.error.message,
        });
      },
      complete: () => {
        this.spinnerService.hide();
      }
    })
  }


  onActivityTemplateSelect(template: ActivityTemplateDto) {
    this.selectedTemplate = template;
    if (this.selectedTeam) {
      this.control(this.eventFormGroup, 'title').setValue(this.selectedTeam?.name + ' | ' + this.selectedTemplate.name);
    }
    this.control(this.eventFormGroup, 'description').setValue(template.description);
  }

  onSelectTeam(groupSelection: OrganizationGroupSelection) {
    this.selectedTeam = groupSelection.team;
    if (this.selectedTemplate) {
      this.control(this.eventFormGroup, 'title').setValue(this.selectedTeam?.name + ' | ' + this.selectedTemplate.name);
    }
  }


  onSelectEventRecurrence() {
    const ref = this.dialogService.open(SelectRecurenceDialogComponent, {
      width: '35vw',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: 'Select recurrence',
      data: {
        selectedRule: this.control(this.eventFormGroup, 'recurrence').value,
        startDate: this.control(this.eventFormGroup, 'startDate').value
      }
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: (selectedRule) => {
          if (selectedRule) {
            this.control(this.eventFormGroup, 'recurrence').setValue(selectedRule);
            this.control(this.eventFormGroup, 'recurrenceText').setValue(translateRRule(selectedRule));
          }
        }
      })
  }

  onClearEventRecurrence() {
    this.control(this.eventFormGroup, 'recurrence').reset();
    this.control(this.eventFormGroup, 'recurrenceText').reset();
  }

  onSidebarHide() {
    this.sidebarStateChange.emit(false);
  }

  onSidebarShow() {
    this.sidebarStateChange.emit(true);
  }

  onSelectTimeSlot() {
    const profiles: TeamMemberDto[] = this.control(this.eventFormGroup, 'attendees').value || [];
    const invitedUsers = profiles.map(value => value.profile);
    const startDate = this.control(this.eventFormGroup, 'startDate').value;
    if (!invitedUsers || invitedUsers.length == 0 || !startDate) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Unable to find time slot',
        detail: 'Select first meeting date and attendees',
      });
      return;
    }
    invitedUsers.push(this.authService.getCurrentUser()!);

    const ref = this.dialogService.open(SelectTimeSlotDialogComponent, {
      width: '45vw',
      breakpoints: {
        '1199px': '55vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: 'Find time slot',
      data: {
        startDate: startDate,
        invitedUsers: invitedUsers
      }
    });
    ref.onClose
      .pipe(filter(Boolean), map(value => value as TimeSlotDto))
      .subscribe({
        next: (selectedTimeSlot) => {
          if (!selectedTimeSlot.isOccupied) {
            this.messageService.add({
              severity: 'success',
              summary: 'Time slot selected',
              detail: 'Your time slot is successfully selected',
            });
          } else {
            this.messageService.add({
              severity: 'warn',
              summary: 'Attention conflict event',
              detail: 'Your time slot is already occupied with another event',
            });
          }
          this.control(this.eventFormGroup, 'startTime').setValue(new Date(selectedTimeSlot.startTime));
          this.control(this.eventFormGroup, 'endTime').setValue(new Date(selectedTimeSlot.endTime));
        }
      })
  }

  protected readonly control = control;
  protected readonly conferenceProviders = conferenceProviders;
  protected readonly calendarProviders = calendarProviders;
  protected readonly currentDate = currentDate;
  protected readonly isSameDay = isSameDay;
}
