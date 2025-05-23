import { Component, Input } from '@angular/core';
import { MeterProgressMetadata, TeamLoadMetadata, TeamMemberLoadMetadata } from '../../../../model/activity-room.model';

@Component({
  selector: 'team-capacity-table',
  templateUrl: './team-capacity-table.component.html',
  styleUrl: './team-capacity-table.component.css'
})
export class TeamCapacityTableComponent {
  @Input() teamLoadMetadata?: TeamLoadMetadata;


  getMeterGroup(progressMetadata: MeterProgressMetadata) {
    return [
      {
        label: 'Todo',
        color: '#60a5fa',
        value: progressMetadata?.todoPercentage || 0.0,
        icon: 'pi pi-folder-open'
      },
      {
        label: 'Progress',
        color: '#fbbf24',
        value: progressMetadata?.inProgressPercentage || 0.0,
        icon: 'pi pi-sync'
      },
      {
        label: 'Done',
        color: '#34d399',
        value: progressMetadata?.donePercentage || 0.0,
        icon: 'pi pi-check-circle'
      },
    ]
  }
}
