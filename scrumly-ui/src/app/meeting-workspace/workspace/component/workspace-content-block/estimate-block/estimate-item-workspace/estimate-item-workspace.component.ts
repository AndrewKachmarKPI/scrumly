import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import {
  ActivityEstimateBlock, ActivityRoomStatusMetadata,
  EstimateIssue,
  UserEstimateMetadata,
  UserMetadata
} from '../../../../model/activity-room.model';
import { ActivityRoomActionsWsService } from '../../../../services/activity-room-actions-ws.service';
import { ActivityRoomActionsService } from '../../../../services/activity-room-actions.service';
import { MessageService } from 'primeng/api';
import { FormControl, Validators } from '@angular/forms';
import { asControl, specialCharacterValidator } from '../../../../../../ui-components/services/utils';

@Component({
  selector: 'estimate-item-workspace',
  templateUrl: './estimate-item-workspace.component.html',
  styleUrl: './estimate-item-workspace.component.css'
})
export class EstimateItemWorkspaceComponent implements OnInit, OnChanges {
  @Input() issue?: EstimateIssue;
  @Input() block?: ActivityEstimateBlock;
  @Input() activityId?: string;
  @Input() userMetadata?: UserMetadata;
  @Input() numberOfUsers?: number;
  @Input() isFacilitator?: boolean;
  @Input() activityRoomStatus?: ActivityRoomStatusMetadata;

  isShowSpinner: boolean = false;

  userEstimate?: UserEstimateMetadata;

  issueEstimatePolarChartData: any;
  issueEstimatePolarChartOptions: any;
  issueEstimatePolarChartHasData: boolean = false;

  finalEstimateControl = new FormControl('', Validators.compose([
    Validators.required, specialCharacterValidator, Validators.maxLength(3)
  ]));
  isEditEstimate: boolean = false;

  constructor(private activityRoomActionServiceWs: ActivityRoomActionsWsService,
              private activityRoomActionService: ActivityRoomActionsService,
              private messageService: MessageService) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['block'] && changes['block'].currentValue) {
      this.onBlockUpdate();
    }
    if (changes['issue'] && changes['issue'].currentValue) {
      this.onIssueUpdate();
    }
  }


  onBlockUpdate() {
    this.userEstimate = this.issue?.userEstimateMetadata
      .find(user => user.userMetadata.userId === this.userMetadata?.userId);
  }

  onIssueUpdate() {
    if (this.issue?.isRevealed) {
      this.initIssueEstimatePolarChart();
    }
    if (this.issue?.finalEstimate) {
      this.isEditEstimate = false;
      this.finalEstimateControl.setValue(this.issue.finalEstimate);
    } else {
      if (this.issue?.isRevealed) {
        this.isEditEstimate = true;
      }
      this.finalEstimateControl.reset();
    }
    if (!this.isFacilitator) {
      this.finalEstimateControl.disable();
    }
  }

  onRemoveEstimateIssue() {
    const issues = [ this.issue?.id! ];
    this.isShowSpinner = true;
    this.activityRoomActionService.onDeleteEstimateIssues(this.activityId!, this.block?.metadata.id!, issues)
      .subscribe({
        next: () => {
          this.isShowSpinner = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Issue deleted',
            detail: 'Your issue successfully deleted',
          });
        },
        error: () => {
          this.isShowSpinner = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Delete failed',
            detail: 'Failed to delete issue',
          });
        }
      })
  }

  onRefreshEstimateIssue() {
    const issues = [ this.issue?.id! ];
    this.isShowSpinner = true;
    this.activityRoomActionService.onUpdateEstimateIssues(this.activityId!, this.block?.metadata.id!, issues)
      .subscribe({
        next: () => {
          this.isShowSpinner = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Issue updated',
            detail: 'Your issue successfully updated',
          });
        },
        error: () => {
          this.isShowSpinner = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Update failed',
            detail: 'Failed to update issue',
          });
        }
      })
  }

  onSelectEstimate(estimate: string) {
    const userEstimate: UserEstimateMetadata = {
      estimate: estimate,
      userMetadata: this.userMetadata!
    }
    this.activityRoomActionServiceWs.onSelectEstimate(this.activityId!, this.block?.metadata.id!, this.issue?.id!, userEstimate);
  }

  onRevealEstimates() {
    this.activityRoomActionServiceWs.onRevealEstimates(this.activityId!, this.block?.metadata.id!, this.issue?.id!)
      .subscribe({
        next: () => {
          this.isEditEstimate = true;
        }
      })
  }

  onHideEstimates() {
    this.activityRoomActionServiceWs.onHideEstimates(this.activityId!, this.block?.metadata.id!, this.issue?.id!);
  }

  onSelectFinalEstimate() {
    if (this.finalEstimateControl.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Invalid estimate',
        detail: `Review final estimate provided`,
      });
      return;
    }
    const finalEstimate = this.finalEstimateControl.value;
    if (finalEstimate === this.issue?.finalEstimate) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Estimate not changed',
        detail: `Chanege estimate first`,
      });
      return;
    }
    this.activityRoomActionServiceWs.onSelectFinalEstimate(this.activityId!, this.block?.metadata.id!, this.issue?.id!, finalEstimate!)
      .subscribe({
        next: () => {
          this.isEditEstimate = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Estimate saved',
            detail: `Final estimate provided saved`,
          });
        }
      })
  }

  onCancelFinalEstimate() {
    this.isEditEstimate = false;
    this.finalEstimateControl.setValue(this.issue?.finalEstimate!);
  }

  initIssueEstimatePolarChart() {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    const data = this.issue?.userEstimateMetadata!.map(user => user.estimate);
    const labels = this.issue?.userEstimateMetadata!
      .map(user => user.userMetadata.firstName + ' ' + user.userMetadata.lastName);

    const oldData = this.issueEstimatePolarChartData &&
    this.issueEstimatePolarChartData?.datasets &&
    this.issueEstimatePolarChartData?.datasets[0].data
      ? JSON.stringify(this.issueEstimatePolarChartData?.datasets[0].data)
      : [];
    const newData = JSON.stringify(data);
    if (oldData !== newData) {
      this.issueEstimatePolarChartHasData = data!.every(value => !isNaN(Number(value)));
      this.issueEstimatePolarChartData = {
        datasets: [
          {
            data: data,
            backgroundColor: [
              documentStyle.getPropertyValue('--yellow-400'),
              documentStyle.getPropertyValue('--green-400'),
              documentStyle.getPropertyValue('--blue-400'),
              documentStyle.getPropertyValue('--cyan-400'),
              documentStyle.getPropertyValue('--red-400'),
              documentStyle.getPropertyValue('--pink-400'),
              documentStyle.getPropertyValue('--bluegray-400'),
            ],
            label: 'Estimate'
          }
        ],
        labels: labels
      }
      this.issueEstimatePolarChartOptions = {
        plugins: {
          legend: {
            labels: {
              color: textColor
            }
          }
        },
        scales: {
          r: {
            grid: {
              color: surfaceBorder
            }
          }
        }
      };
    }

  }

  protected readonly asControl = asControl;
}
