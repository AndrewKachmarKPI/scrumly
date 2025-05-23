import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { HttpClient } from '@angular/common/http';
import { IssueShortInfo } from '../../../../model/issues.model';
import { ActivityRoomActionsService } from '../../../../services/activity-room-actions.service';
import { TeamMetadata } from '../../../../model/activity-room.model';

@Component({
  selector: 'select-backlog-items-dialog',
  templateUrl: './select-backlog-items-dialog.component.html',
  styleUrl: './select-backlog-items-dialog.component.css'
})
export class SelectBacklogItemsDialogComponent implements OnInit {
  teamMetadata?: TeamMetadata;
  blockId?: string;
  activityId?: string;

  isLoadIssues: boolean = false;

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              private activityRoomActionService: ActivityRoomActionsService) {
    this.teamMetadata = this.config.data.teamMetadata;
    this.blockId = this.config.data.blockId;
    this.activityId = this.config.data.activityId;
  }

  ngOnInit(): void {

  }

  onSelectIssues(selectedIssues: IssueShortInfo[]) {
    this.activityRoomActionService.onSelectBoardBacklogIssues(this.activityId!, this.blockId!, selectedIssues)
      .subscribe({
        next: () => {
          this.isLoadIssues = false;
          this.ref.close();
        },
        error: () => {
          this.isLoadIssues = false;
        },
        complete: () => {
          this.isLoadIssues = false;
        }
      });
  }
}
