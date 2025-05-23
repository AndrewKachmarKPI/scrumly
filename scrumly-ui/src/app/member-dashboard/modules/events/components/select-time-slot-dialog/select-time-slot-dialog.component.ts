import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { ActivityService } from "../../services/activity.service";
import { FindTimeSlotRQ, TimeSlotDto, TimeSlotGroupDto } from "../../model/activity.model";
import { control } from "../../../../../ui-components/services/utils";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import moment from "moment-timezone";
import { UserProfileDto } from "../../../../../auth/auth.model";

@Component({
  selector: 'app-select-time-slot-dialog',
  templateUrl: './select-time-slot-dialog.component.html',
  styleUrl: './select-time-slot-dialog.component.css'
})
export class SelectTimeSlotDialogComponent implements OnInit {
  startDate?: Date;
  invitedUsers?: UserProfileDto[] = [];
  timeSlotGroups: TimeSlotGroupDto[] = [];
  searchSlotGroup!: FormGroup;

  loading: boolean = false;

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              private activityService: ActivityService) {
    this.startDate = this.config.data.startDate;
    this.invitedUsers = this.config.data.invitedUsers;

    const startDate = new Date();
    const startDateTime = moment(startDate).set({
      hour: 9,
      minute: 0,
      second: 0,
      millisecond: 0,
    }).toDate();
    const endDateTime = moment(startDate).set({
      hour: 18,
      minute: 0,
      second: 0,
      millisecond: 0,
    }).toDate();
    this.searchSlotGroup = new FormGroup({
      dayStart: new FormControl(startDateTime, Validators.required),
      dayEnd: new FormControl(endDateTime, Validators.required),
      duration: new FormControl(30, Validators.compose([
        Validators.required, Validators.min(1), Validators.max(1440)
      ]))
    });
  }

  ngOnInit(): void {
    this.findTimeSlots();
  }

  findTimeSlots() {
    if (!this.searchSlotGroup.valid) {
      return;
    }
    const start = control(this.searchSlotGroup, 'dayStart').value;
    const end = control(this.searchSlotGroup, 'dayEnd').value;
    const dateZone = moment(this.startDate).tz(moment.tz.guess()).tz();
    const rq: FindTimeSlotRQ = {
      startDate: this.startDate!,
      endDate: this.startDate!,
      minDayTime: start,
      maxDayTime: end,
      meetingDuration: control(this.searchSlotGroup, 'duration').value,
      invitedUsers: this.invitedUsers!,
      userTimeZone: dateZone!
    }
    this.loading = true;
    this.activityService.findTimeSlotRQ(rq).subscribe({
      next: (slotGroups) => {
        this.timeSlotGroups = slotGroups;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    })
  }

  onSelectTimeSlot(timeSlot: TimeSlotDto) {
    this.ref.close(timeSlot);
  }


  protected readonly control = control;
}
