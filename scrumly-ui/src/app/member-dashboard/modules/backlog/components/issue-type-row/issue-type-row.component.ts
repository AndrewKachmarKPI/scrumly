import { Component, Input } from '@angular/core';
import { IssueTypeDto } from '../../model/backlog.model';

@Component({
  selector: 'issue-type-row',
  templateUrl: './issue-type-row.component.html',
  styleUrl: './issue-type-row.component.css'
})
export class IssueTypeRowComponent {
  @Input() issueType?: IssueTypeDto;
  @Input() styleClass: string = "";

  getImageUrl(url: string | undefined): string {
    if (!url) {
      return '';
    }
    if (url.includes('http') || url.includes('https')) {
      return url;
    }
    return window.location.origin + '/' + url;
  }

}
