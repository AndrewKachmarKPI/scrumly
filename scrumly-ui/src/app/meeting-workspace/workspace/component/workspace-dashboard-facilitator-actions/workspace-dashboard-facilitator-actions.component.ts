import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  ActivityBlock,
  ActivityEstimateBlock,
  ActivityRoom,
  ActivityTimerState, EstimateIssue, EstimateScaleMetadata
} from '../../model/activity-room.model';
import { ActivityRoomActionsWsService } from '../../services/activity-room-actions-ws.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { control } from '../../../../ui-components/services/utils';
import { FormControl } from '@angular/forms';
import { ActivityBlockType } from '../../../../member-dashboard/modules/events/model/activity.model';
import { ActivityRoomActionsService } from '../../services/activity-room-actions.service';
import { ActivityRoomService } from '../../services/activity-room.service';
import { SpinnerService } from '../../../../ui-components/services/spinner.service';
import {
  OrgCreateDialogComponent
} from '../../../../member-dashboard/modules/organizations/dialogs/org-create-dialog/org-create-dialog.component';
import { filter } from 'rxjs';
import { DialogService } from 'primeng/dynamicdialog';
import { SyncBlockDialogComponent } from '../sync-block-dialog/sync-block-dialog.component';

@Component({
  selector: 'workspace-dashboard-facilitator-actions',
  templateUrl: './workspace-dashboard-facilitator-actions.component.html',
  styleUrl: './workspace-dashboard-facilitator-actions.component.css'
})
export class WorkspaceDashboardFacilitatorActionsComponent implements OnInit {
  @Input() activityRoom?: ActivityRoom;
  @Input() activityTimerState?: ActivityTimerState;
  @Input() showFacilitatorActions: boolean = true;
  @Output() showFacilitatorActionsChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  @Input() meetingNotesState: boolean = false;
  @Output() meetingNotesStateChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  timerDurationControl = new FormControl(60);
  timerDurationOptions = [
    { label: '30 seconds', value: 30 },
    { label: '1 minute', value: 60 },
    { label: '2 minute', value: 120 },
    { label: '5 minutes', value: 300 },
    { label: '10 minutes', value: 600 }
  ];

  constructor(private roomActionsServiceWs: ActivityRoomActionsWsService,
              private activityRoomService: ActivityRoomService,
              private spinnerService: SpinnerService,
              private confirmationService: ConfirmationService,
              private messageService: MessageService,
              private dialogService: DialogService) {
  }

  ngOnInit(): void {
  }

  onFinishMeeting(event: Event) {
    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: 'Are you sure you want to finish meeting?',
      icon: 'pi pi-info-circle',
      acceptButtonStyleClass: 'p-button-success p-button-sm',
      accept: () => {
        this.finishMeeting();
      }
    });
  }

  finishMeeting() {
    this.spinnerService.show();
    this.activityRoomService.finishActivityRoom(this.activityRoom?.activityId!)
      .subscribe({
        next: () => {
          this.spinnerService.hide();
          this.messageService.add({
            severity: 'success',
            summary: 'Activity closed',
            detail: 'You have successfully closed activity',
          });
        },
        error: () => {
          this.spinnerService.hide();
          this.messageService.add({
            severity: 'error',
            summary: 'Activity close failed',
            detail: 'Failed to close this activity',
          });
        }
      })
  }

  onNextBlock() {
    const currentlyActive = this.activityRoom?.blockNavigationMetadata.activeBlockId;
    const blocks = this.activityRoom?.activityBlocks;
    if (!blocks || blocks.length === 0) {
      return;
    }

    const currentBlock = this.activityRoom?.activityBlocks!
      .find(block => block.metadata.id === currentlyActive);
    if (currentBlock?.metadata.type === ActivityBlockType.ESTIMATE_BLOCK) {
      const estimateBlock = currentBlock as ActivityEstimateBlock;
      const { estimateIssues, activeEstimateIssueId } = estimateBlock;
      if (!estimateIssues || estimateIssues.length === 0) {
        this.navigateNextBlock(blocks, currentlyActive!);
        return;
      }
      const currentIssueIndex = estimateIssues.findIndex(issue => issue.id === activeEstimateIssueId);
      if (currentIssueIndex !== -1 && currentIssueIndex < estimateIssues.length - 1) {
        const issueId = estimateIssues[currentIssueIndex + 1].id;
        this.roomActionsServiceWs.onChangeActiveEstimateIssue(this.activityRoom?.activityId!, currentBlock.metadata.id, issueId);
      } else {
        this.navigateNextBlock(blocks, currentlyActive!);
      }
    } else {
      this.navigateNextBlock(blocks, currentlyActive!);
    }
  }

  navigateNextBlock(blocks: ActivityBlock[], currentlyActive: string) {
    const currentIndex = blocks.findIndex(value => value.metadata.id === currentlyActive);
    const nextIndex = (currentIndex + 1) % blocks.length;
    const nextBlock = blocks[nextIndex];
    this.activityRoom!.blockNavigationMetadata!.activeBlockId = nextBlock.metadata.id;
    this.roomActionsServiceWs.onActivityRoomNavigationChange(this.activityRoom?.activityId!, this.activityRoom!.blockNavigationMetadata!).subscribe();
  }

  onPreviousBlock() {
    const currentlyActive = this.activityRoom?.blockNavigationMetadata.activeBlockId;
    const blocks = this.activityRoom?.activityBlocks;
    if (!blocks || blocks.length === 0) {
      return;
    }

    const currentBlock = blocks.find(block => block.metadata.id === currentlyActive);
    if (currentBlock?.metadata.type === ActivityBlockType.ESTIMATE_BLOCK) {
      const estimateBlock = currentBlock as ActivityEstimateBlock;
      const { estimateIssues, activeEstimateIssueId } = estimateBlock;
      if (!estimateIssues || estimateIssues.length === 0) {
        this.navigatePrevBlock(blocks, currentlyActive!);
        return;
      }
      const currentIssueIndex = estimateIssues.findIndex(issue => issue.id === activeEstimateIssueId);

      if (currentIssueIndex > 0) {
        const issueId = estimateIssues[currentIssueIndex - 1].id;
        this.roomActionsServiceWs.onChangeActiveEstimateIssue(this.activityRoom?.activityId!, currentBlock.metadata.id, issueId);
      } else {
        this.navigatePrevBlock(blocks, currentlyActive!);
      }
    } else {
      this.navigatePrevBlock(blocks, currentlyActive!);
    }
  }


  navigatePrevBlock(blocks: ActivityBlock[], currentlyActive: string) {
    const currentIndex = blocks.findIndex(value => value.metadata.id === currentlyActive);
    const prevIndex = (currentIndex - 1 + blocks.length) % blocks.length;
    const prevBlock = blocks[prevIndex];
    this.activityRoom!.blockNavigationMetadata!.activeBlockId = prevBlock.metadata.id;
    this.roomActionsServiceWs.onActivityRoomNavigationChange(this.activityRoom?.activityId!, this.activityRoom!.blockNavigationMetadata!).subscribe();
  }

  onStartTimer() {
    if (this.timerDurationControl.invalid) {
      return;
    }
    this.activityTimerState = {
      state: true,
      timeout: this.timerDurationControl.value!
    }
    this.roomActionsServiceWs.onChangeTimerState(this.activityRoom?.activityId!, this.activityTimerState);
  }

  onStopTimer() {
    this.activityTimerState = {
      state: false
    }
    this.roomActionsServiceWs.onChangeTimerState(this.activityRoom?.activityId!, this.activityTimerState);
  }


  onToggleMeetingNotes() {
    this.meetingNotesState = true;
    this.meetingNotesStateChange.emit(this.meetingNotesState);
  }

  toggleFacilitatorActions() {
    this.showFacilitatorActions = !this.showFacilitatorActions;
    this.showFacilitatorActionsChange.emit(this.showFacilitatorActions);
  }

  onToggleSync() {
    const ref = this.dialogService.open(SyncBlockDialogComponent, {
      width: '45vw',
      breakpoints: {
        '1199px': '75vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      closeOnEscape: true,
      closable: true,
      header: 'Sync activity block',
      data: {
        activityRoom: this.activityRoom
      }
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: () => {

        }
      })
  }

  protected readonly control = control;
}
