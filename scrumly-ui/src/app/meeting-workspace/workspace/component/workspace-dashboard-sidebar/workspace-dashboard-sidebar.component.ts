import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ActivityBlockConfigDto, ActivityDto } from '../../../../member-dashboard/modules/events/model/activity.model';
import { ActivityRoom } from '../../model/activity-room.model';

@Component({
  selector: 'workspace-dashboard-sidebar',
  templateUrl: './workspace-dashboard-sidebar.component.html',
  styleUrl: './workspace-dashboard-sidebar.component.css'
})
export class WorkspaceDashboardSidebarComponent implements OnInit {
  @Input() activityRoom?: ActivityRoom;
  @Input() sideBarState?: boolean;

  @Output() sideBarStateChange: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() selectedBlockIdChange: EventEmitter<string> = new EventEmitter<string>();

  @Input() selectedBlockId?: string;
  @Input() isFacilitator?: boolean;

  @Input() nestedBlockId?: string;
  @Output() nestedBlockIdChange: EventEmitter<string> = new EventEmitter<string>();

  ngOnInit(): void {
  }

  constructor() {
  }

  toggleSidebar() {
    this.sideBarState = !this.sideBarState;
    this.sideBarStateChange.emit(this.sideBarState);
  }

  onSelectedBlockIdChange(selectedBlockId: string) {
    this.selectedBlockId = selectedBlockId;
    this.selectedBlockIdChange.emit(this.selectedBlockId);
  }

  onNestedBlockIdChange(nestedBlockId: string) {
    this.nestedBlockId = nestedBlockId;
    this.nestedBlockIdChange.emit(this.nestedBlockId);
  }
}
