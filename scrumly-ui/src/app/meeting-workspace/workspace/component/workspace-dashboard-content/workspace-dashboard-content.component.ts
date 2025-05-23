import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { ActivityDto } from '../../../../member-dashboard/modules/events/model/activity.model';
import { ActivityBlock, ActivityRoom, UserMetadata } from '../../model/activity-room.model';

@Component({
  selector: 'workspace-dashboard-content',
  templateUrl: './workspace-dashboard-content.component.html',
  styleUrl: './workspace-dashboard-content.component.css'
})
export class WorkspaceDashboardContentComponent implements OnInit, OnChanges, OnDestroy {
  @Input() activityRoom?: ActivityRoom;
  @Input() activeBlockId?: string;
  @Input() sideBarState?: boolean;
  @Output() sideBarStateChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  @Input() zoomLevel = 1;
  @Output() zoomLevelChange: EventEmitter<number> = new EventEmitter<number>();

  @Input() selectedBlockId?: string;
  @Input() nestedBlockId?: string;
  @Input() isFacilitator?: boolean;
  activeBlock?: ActivityBlock;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['activityRoom'] && changes['activityRoom'].currentValue) {
      this.onActivityRoomChange();
    }
    if (changes['selectedBlockId'] && changes['selectedBlockId'].currentValue) {
      this.onActivityRoomChange();
    }
  }

  ngOnDestroy(): void {
  }

  ngOnInit(): void {
  }

  onActivityRoomChange() {
    this.activeBlock = this.activityRoom?.activityBlocks!
      .find(block => block.metadata.id === this.selectedBlockId);
  }

  toggleSidebar() {
    this.sideBarState = !this.sideBarState;
    this.sideBarStateChange.emit(this.sideBarState);
  }

  onZoomLevelChange(zoomLevel: number) {
    this.zoomLevel = zoomLevel;
    this.zoomLevelChange.emit(this.zoomLevel);
  }
}
