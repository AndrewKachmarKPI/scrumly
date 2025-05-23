import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import {
  ActivityBlock, ActivityEstimateBlock,
  ActivityQuestionBlock, ActivityRoomStatusMetadata, EstimateIssue, TeamMetadata,
  UserMetadata
} from '../../../model/activity-room.model';
import { IssueShortInfo } from '../../../model/issues.model';
import { ActivityRoomActionsWsService } from '../../../services/activity-room-actions-ws.service';
import { ActivityRoomActionsService } from '../../../services/activity-room-actions.service';

@Component({
  selector: 'estimate-block',
  templateUrl: './estimate-block.component.html',
  styleUrl: './estimate-block.component.css'
})
export class EstimateBlockComponent implements OnInit {
  @Input('block') set estimateBlock(block: ActivityBlock) {
    this.block = block as ActivityEstimateBlock;
  }

  block!: ActivityEstimateBlock;

  @Input() activityId?: string;
  @Input() isFacilitator?: boolean;
  @Input() userMetadata?: UserMetadata;
  @Input() teamMetadata?: TeamMetadata;
  @Input() numberOfUsers?: number;
  @Input() nestedBlockId?: string;
  @Input() activityRoomStatus?: ActivityRoomStatusMetadata;

  isSearchIssues: boolean = true;
  isLoadIssues: boolean = false;
  activeEstimateIssue?: EstimateIssue;


  constructor(private activityRoomActionService: ActivityRoomActionsService) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['estimateBlock'] && changes['estimateBlock'].firstChange && this.block) {
      this.onBlockChange();
    }
    if (changes['estimateBlock'] && changes['estimateBlock'].currentValue && this.block) {
      this.onBlockChange();
    }
    if (changes['nestedBlockId'] && changes['nestedBlockId'].currentValue && this.nestedBlockId) {
      this.onSelectedNestedIdChange();
    }
  }

  onBlockChange() {
    this.isSearchIssues = !this.block.activeEstimateIssueId;
    this.activeEstimateIssue = this.block.estimateIssues
      .find(issue => issue.id === this.block.activeEstimateIssueId)!;
  }

  onSelectedNestedIdChange() {
    this.activeEstimateIssue = this.block.estimateIssues
      .find(issue => issue.id === this.nestedBlockId)!;
  }

  onSelectedIssues(selectedIssues: IssueShortInfo[]) {
    this.activityRoomActionService.onSelectEstimateIssues(this.activityId!, this.block?.metadata?.id!, selectedIssues)
      .subscribe({
        next: () => {
          this.isLoadIssues = false;
        },
        error: () => {
          this.isLoadIssues = false;
        }
      });
  }
}
