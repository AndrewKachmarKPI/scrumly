import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { getInviteSeverity } from "../../../../../ui-components/services/utils";
import { InviteDto, InviteHistoryDto, InviteStatus } from "../../../invites/model/invite.model";

@Component({
  selector: 'org-invite-history-dialog',
  templateUrl: './org-invite-history-dialog.component.html',
  styleUrl: './org-invite-history-dialog.component.css'
})
export class OrgInviteHistoryDialogComponent implements OnInit {
  invite?: InviteDto;

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,) {
    this.invite = this.config.data.invite;
  }

  ngOnInit(): void {
  }

  public inviteChangeLog(): InviteHistoryDto[] {
    if (!this.invite) {
      return [];
    }
    return this.invite?.changeLog.sort((a, b) => {
      const dateA = new Date(a.dateTime!).getTime();
      const dateB = new Date(b.dateTime!).getTime();
      return dateB - dateA;
    }) || [];
  }

  getSeverity(status: InviteStatus): string {
    const statusMap: Record<InviteStatus, string> = {
      [InviteStatus.NEW]: 'blue-500',
      [InviteStatus.RESENT]: 'blue-500',
      [InviteStatus.REJECTED]: 'red-500',
      [InviteStatus.ACCEPTED]: 'green-500',
      [InviteStatus.EXPIRED]: 'yellow-500',
      [InviteStatus.REVOKED]: 'gray-500'
    };
    return statusMap[status] || 'green-500';
  }

  protected readonly getInviteSeverity = getInviteSeverity;
}
