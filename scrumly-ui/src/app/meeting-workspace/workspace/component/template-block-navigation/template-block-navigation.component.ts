import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {
  ActivityBlockConfigDto, ActivityBlockType,
  ActivityTemplateDto
} from '../../../../member-dashboard/modules/events/model/activity.model';
import { getBlockColor, getBlockIcon, trackById } from '../../../../ui-components/services/utils';
import {
  ActivityBlock,
  ActivityBlockMetadata, ActivityEstimateBlock,
  ActivityRoom,
  BlockNavigationMetadata, EstimateIssue
} from '../../model/activity-room.model';
import { ActivityRoomActionsService } from '../../services/activity-room-actions.service';
import { AuthService } from '../../../../auth/auth.service';
import { Dropdown, DropdownChangeEvent } from 'primeng/dropdown';
import { ActivityRoomActionsWsService } from '../../services/activity-room-actions-ws.service';
import { FormControl } from '@angular/forms';
import {
  ActivityBlockSortPipe
} from '../../../../ui-components/scrumly-components/prime-utils/pipes/activity-block-sort.pipe';


@Component({
  selector: 'template-block-navigation',
  templateUrl: './template-block-navigation.component.html',
  styleUrl: './template-block-navigation.component.css',
  providers: [ ActivityBlockSortPipe ]
})
export class TemplateBlockNavigationComponent implements OnInit, OnChanges, OnDestroy {
  @Input() activityRoom?: ActivityRoom;
  @Input() mode: 'list' | 'popup' = 'list';
  @Input() isFacilitator?: boolean;

  @Input() selectedBlockId?: string;
  @Output() selectedBlockIdChange: EventEmitter<string> = new EventEmitter<string>();

  @Input() nestedBlockId?: string;
  @Output() nestedBlockIdChange: EventEmitter<string> = new EventEmitter<string>();

  @ViewChild('dropdown') dropdown?: Dropdown;

  activeBlock?: ActivityBlockMetadata;
  currentActiveBlock?: ActivityBlockMetadata;

  menuOptions: ActivityBlockMetadata[] = [];
  nestedMenuOptions: Map<string, EstimateIssue[]> = new Map<string, EstimateIssue[]>();

  constructor(private roomActionsService: ActivityRoomActionsWsService,
              private activitySortPipe: ActivityBlockSortPipe,
              private authService: AuthService) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activityRoom'] && changes['activityRoom'].firstChange) {
      this.onRoomChange();
    }
    if (changes['activityRoom'] && changes['activityRoom'].currentValue) {
      this.updateNestedMenuOptions();
    }
    if (changes['selectedBlockId'] && changes['selectedBlockId'].currentValue) {
      this.onRoomChange();
    }
  }

  ngOnDestroy(): void {
  }

  onSelectBlock(block: ActivityBlockMetadata) {
    if (block.id === this.selectedBlockId) {
      return;
    }
    this.selectedBlockId = block.id;
    if (this.activityRoom?.statusMetadata?.isActive) {
      this.roomActionsService.onUserNavigationChange(this.activityRoom?.activityId!, this.selectedBlockId);
    } else {
      if (block.type !== ActivityBlockType.ESTIMATE_BLOCK) {
        this.nestedBlockId = undefined;
        this.nestedBlockIdChange.emit(this.nestedBlockId);
      } else if (this.nestedMenuOptions.has(block.id)) {
        const issues = this.nestedMenuOptions.get(block.id) || [];
        if (issues) {
          this.nestedBlockId = issues[0].id;
          this.nestedBlockIdChange.emit(this.nestedBlockId);
        }
      }
      this.selectedBlockIdChange.emit(this.selectedBlockId);
    }
  }

  onSelectBlockChange(event: DropdownChangeEvent) {
    this.onSelectBlock(event.value as ActivityBlockMetadata);
  }

  isActiveBlock(block: ActivityBlockMetadata) {
    return this.activityRoom?.statusMetadata?.isActive &&
      this.activityRoom!.blockNavigationMetadata!.activeBlockId === block.id;
  }

  isSelectedBlock(block: ActivityBlockMetadata) {
    return !this.isActiveBlock(block) && this.selectedBlockId === block.id;
  }

  onRoomChange() {
    this.menuOptions = this.activitySortPipe.transform(this.activityRoom!.blockNavigationMetadata.blocks);
    this.currentActiveBlock = this.menuOptions
      .find(block => block.id === this.activityRoom!.blockNavigationMetadata.activeBlockId);
    this.activeBlock = this.menuOptions
      .find(block => block.id === this.selectedBlockId) || this.currentActiveBlock;
    this.updateNestedMenuOptions();

    if (!this.activityRoom?.statusMetadata?.isActive && !this.selectedBlockId && this.menuOptions.length > 0) {
      this.selectedBlockId = this.menuOptions[0].id;
      this.selectedBlockIdChange.emit(this.selectedBlockId);

      this.currentActiveBlock = this.menuOptions[0];
      this.activeBlock = this.menuOptions[0];
    }
  }


  updateNestedMenuOptions() {
    this.activityRoom?.activityBlocks.forEach(block => {
      if (block.metadata.type === ActivityBlockType.ESTIMATE_BLOCK) {
        const newOptions = this.getEstimateBlockNestedOptions(block.metadata);
        this.nestedMenuOptions.set(block.metadata.id, newOptions);
      }
    })
  }

  getEstimateBlockNestedOptions(block: ActivityBlockMetadata): EstimateIssue[] {
    if (block.type === ActivityBlockType.ESTIMATE_BLOCK) {
      const estimateBlock: ActivityEstimateBlock = this.activityRoom!.activityBlocks
        .find(activityBlock => activityBlock.metadata.id == block.id) as ActivityEstimateBlock;
      return estimateBlock.estimateIssues
    }
    return [];
  }

  isEstimateIssueActive(block: ActivityBlockMetadata, issue: EstimateIssue) {
    const estimateBlock: ActivityEstimateBlock = this.activityRoom!.activityBlocks
      .find(activityBlock => activityBlock.metadata.id == block.id) as ActivityEstimateBlock;
    return this.isActiveBlock(block) && estimateBlock.activeEstimateIssueId === issue.id;
  }

  isEstimateIssueSelected(issue: EstimateIssue) {
    return issue.id === this.nestedBlockId;
  }

  onSelectEstimateIssue(block: ActivityBlockMetadata, issue: EstimateIssue) {
    if (this.activityRoom?.statusMetadata?.isActive) {
      this.roomActionsService.onChangeActiveEstimateIssue(this.activityRoom?.activityId!, block.id, issue.id);
    } else {
      this.selectedBlockId = block.id;
      this.selectedBlockIdChange.emit(this.selectedBlockId);

      this.nestedBlockId = issue.id;
      this.nestedBlockIdChange.emit(this.nestedBlockId);
    }
  }

  onOpenSearchEstimateIssue(block: ActivityBlockMetadata) {
    if (this.activityRoom?.statusMetadata?.isActive) {
      this.roomActionsService.onOpenSearchEstimateIssue(this.activityRoom?.activityId!, block.id);
    }
  }

  protected readonly trackById = trackById;
  protected readonly getBlockIcon = getBlockIcon;
  protected readonly ActivityBlockType = ActivityBlockType;
}
