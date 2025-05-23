import { Component, Input } from '@angular/core';
import { IssueShortInfo } from '../../../../model/issues.model';
import { IssueMetadata } from '../../../../model/activity-room.model';

@Component({
  selector: 'estimate-item-row',
  templateUrl: './estimate-item-row.component.html',
  styleUrl: './estimate-item-row.component.css'
})
export class EstimateItemRowComponent {
  @Input() issue?: IssueMetadata | IssueShortInfo;
  getIssueUrl(url: string) {
    if (!url) {
      return '';
    }
    if (url.includes('http') || url.includes('https')) {
      return url;
    }
    return window.location.origin + '/app/backlog/' + url + '/issue';
  }
}
