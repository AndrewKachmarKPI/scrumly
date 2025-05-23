import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ActivityRoom } from '../../model/activity-room.model';
import { trackById, trackByUserId } from '../../../../ui-components/services/utils';

@Component({
  selector: 'workspace-dashboard-content-header',
  templateUrl: './workspace-dashboard-content-header.component.html',
  styleUrl: './workspace-dashboard-content-header.component.css'
})
export class WorkspaceDashboardContentHeaderComponent {
  @Input() activityRoom?: ActivityRoom;
  @Input() isFacilitator: boolean = false;
  @Input() sideBarState?: boolean;
  @Input() selectedBlockId?: string;
  @Input() zoomLevel = 1;

  @Output() sideBarStateChange: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() onExitRoom: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() zoomLevelChange: EventEmitter<number> = new EventEmitter<number>();
  @Output() onKickUser: EventEmitter<string> = new EventEmitter<string>();
  @Output() onViewReport: EventEmitter<boolean> = new EventEmitter<boolean>();

  exitRoom() {
    this.onExitRoom.emit(true);
  }

  viewReport() {
    this.onViewReport.emit(true);
  }

  toggleSidebar() {
    this.sideBarState = !this.sideBarState;
    this.sideBarStateChange.emit(this.sideBarState);
  }

  onZoomLevelChange(zoomLevel: number) {
    this.zoomLevel = zoomLevel;
    this.zoomLevelChange.emit(this.zoomLevel);
  }

  kickFromWorkspace(userId: string) {
    this.onKickUser.emit(userId);
  }

  protected readonly trackByUserId = trackByUserId;
}
