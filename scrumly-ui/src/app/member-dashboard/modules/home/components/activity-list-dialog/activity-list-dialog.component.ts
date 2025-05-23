import { Component } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ActivityDto } from '../../../events/model/activity.model';

@Component({
  selector: 'app-activity-list-dialog',
  templateUrl: './activity-list-dialog.component.html',
  styleUrl: './activity-list-dialog.component.css'
})
export class ActivityListDialogComponent {
  activities: ActivityDto[] = [];

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig) {
    this.activities = this.config.data.activities;
  }

  closeDialog() {
    this.ref.close();
  }
}
