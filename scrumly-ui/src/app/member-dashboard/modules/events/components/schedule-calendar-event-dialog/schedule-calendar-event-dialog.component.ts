import { Component } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { ActivityDto } from "../../model/activity.model";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { calendarProviders, conferenceProviders, control } from "../../../../../ui-components/services/utils";
import { ActivityService } from "../../services/activity.service";
import { CreateActivityRQ, ScheduleActivityCalendarEventRQ } from "../../model/event.model";
import { MessageService } from "primeng/api";
import { SpinnerService } from "../../../../../ui-components/services/spinner.service";

@Component({
  selector: 'app-schedule-calendar-event-dialog',
  templateUrl: './schedule-calendar-event-dialog.component.html',
  styleUrl: './schedule-calendar-event-dialog.component.css'
})
export class ScheduleCalendarEventDialogComponent {
  activity: ActivityDto;
  scheduleCalendarEventForm: FormGroup;

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              private messageService: MessageService,
              private spinnerService: SpinnerService,
              private activityService: ActivityService) {
    this.activity = this.config.data.activity;
    this.scheduleCalendarEventForm = new FormGroup({
      conferenceProvider: new FormControl("", Validators.required),
      calendarProvider: new FormControl("", Validators.required)
    });
  }

  closeDialog() {
    this.ref.close();
  }

  onSubmitForm() {
    if (!this.scheduleCalendarEventForm.valid) {
      return;
    }
    this.spinnerService.show();
    const rq: ScheduleActivityCalendarEventRQ = {
      activityId: this.activity.activityId,
      conferenceProvider: this.control(this.scheduleCalendarEventForm, 'conferenceProvider').value.name,
      calendarProvider: this.control(this.scheduleCalendarEventForm, 'calendarProvider').value.name,
    }
    this.activityService.scheduleActivityCalendarEvent(rq).subscribe({
      next: (activity) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Event scheduled',
          detail: 'You have successfully scheduled event',
        });
        this.ref.close(activity);
      },
      error: () => {
        this.spinnerService.hide();
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to schedule event',
          detail: 'Error while scheduling event',
        })
      },
      complete: () => {
        this.spinnerService.hide();
      }
    })
  }

  protected readonly control = control;
  protected readonly calendarProviders = calendarProviders;
  protected readonly conferenceProviders = conferenceProviders;
}
