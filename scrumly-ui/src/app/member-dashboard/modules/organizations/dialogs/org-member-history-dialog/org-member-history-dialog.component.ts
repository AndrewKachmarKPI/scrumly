import { Component, OnInit } from '@angular/core';
import { InviteDto, InviteHistoryDto, InviteStatus } from "../../../invites/model/invite.model";
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { MemberChangeAction, MemberHistoryDto, MemberStatus } from "../../model/organization.model";
import { getMemberRoleSeverity } from "../../../../../ui-components/services/utils";

@Component({
  selector: 'app-org-member-history-dialog',
  templateUrl: './org-member-history-dialog.component.html',
  styleUrl: './org-member-history-dialog.component.css'
})
export class OrgMemberHistoryDialogComponent implements OnInit {
  memberHistory?: MemberHistoryDto[];

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,) {
    this.memberHistory = this.config.data.changeHistory;
  }

  ngOnInit(): void {
  }

  public changeLog(): MemberHistoryDto[] {
    if (!this.memberHistory) {
      return [];
    }
    return this.memberHistory.sort((a, b) => {
      const dateA = new Date(a.dateTime!).getTime();
      const dateB = new Date(b.dateTime!).getTime();
      return dateB - dateA;
    }) || [];
  }

  getSeverity(status: MemberChangeAction): string {
    const statusMap: Record<MemberChangeAction, string> = {
      [MemberChangeAction.MEMBER_CREATED]: 'blue-500',
      [MemberChangeAction.MEMBER_INVITED]: 'blue-500',
      [MemberChangeAction.MEMBER_JOINED]: 'green-500',
      [MemberChangeAction.MEMBER_BLOCKED]: 'red-500',
      [MemberChangeAction.MEMBER_LEFT]: 'yellow-500',
      [MemberChangeAction.MEMBER_ACTIVATED]: 'green-500',
      [MemberChangeAction.MEMBER_ROLE_CHANGED]: 'gray-500'
    };
    return statusMap[status] || 'green-500';
  }

  protected readonly getMemberRoleSeverity = getMemberRoleSeverity;
}
