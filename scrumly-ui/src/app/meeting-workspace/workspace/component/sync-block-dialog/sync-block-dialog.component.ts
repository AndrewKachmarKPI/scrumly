import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ActivityRoom, SyncBlockOption } from '../../model/activity-room.model';
import { ActivityRoomService } from '../../services/activity-room.service';
import { ActivityRoomActionsWsService } from '../../services/activity-room-actions-ws.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'sync-block-dialog',
  templateUrl: './sync-block-dialog.component.html',
  styleUrl: './sync-block-dialog.component.css'
})
export class SyncBlockDialogComponent implements OnInit {
  syncOptions: SyncBlockOption[] = [];
  activityRoom?: ActivityRoom;
  isLoad?: boolean = false;

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              private activityRoomService: ActivityRoomService,
              private messageService: MessageService,
              private activityRoomActionsWsService: ActivityRoomActionsWsService) {
    this.activityRoom = this.config.data.activityRoom;
  }

  ngOnInit(): void {
    this.loadSyncOptions();
  }


  loadSyncOptions() {
    this.isLoad = true;
    this.activityRoomService.getSyncBlockOptions(this.activityRoom?.activityId!)
      .subscribe({
        next: (syncOptions) => {
          this.isLoad = false;
          this.syncOptions = syncOptions;
        },
        error: () => {
          this.isLoad = false;
        }
      })
  }


  onSubmitForm(syncOption: SyncBlockOption) {
    this.activityRoomActionsWsService.onSyncActivityBlock(this.activityRoom?.activityId!, this.activityRoom?.blockNavigationMetadata.activeBlockId!, syncOption);
    this.closeDialog();
    this.messageService.add({
      severity: 'success',
      summary: 'Block synchronized',
      detail: 'Your block successfully synchronized with previous event',
    });
  }

  closeDialog() {
    this.ref.close(false);
  }

}
