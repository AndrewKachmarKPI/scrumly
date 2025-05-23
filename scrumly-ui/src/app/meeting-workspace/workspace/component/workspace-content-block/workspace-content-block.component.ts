import { Component, Input, OnInit } from '@angular/core';
import {
  ActivityBlock,
  ActivityRoom,
  ActivityRoomStatus, ActivityRoomStatusMetadata,
  TeamMetadata,
  UserMetadata
} from '../../model/activity-room.model';
import { ActivityBlockType } from '../../../../member-dashboard/modules/events/model/activity.model';

@Component({
  selector: 'workspace-content-block',
  templateUrl: './workspace-content-block.component.html',
  styleUrl: './workspace-content-block.component.css'
})
export class WorkspaceContentBlockComponent implements OnInit {
  @Input() activityBlock?: ActivityBlock;
  @Input() activityRoomStatus?: ActivityRoomStatusMetadata;
  @Input() activityId?: string;
  @Input() teamMetadata?: TeamMetadata;
  @Input() userMetadata?: UserMetadata;
  @Input() isFacilitator?: boolean;
  @Input() numberOfUsers?: number;
  @Input() nestedBlockId?: string;


  constructor() {
  }

  ngOnInit(): void {
  }


  protected readonly ActivityBlockType = ActivityBlockType;
}
