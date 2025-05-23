import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { control, lineEmailValidator } from "../../../../../ui-components/services/utils";
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { MessageService } from "primeng/api";
import { OrganizationInviteService } from "../../services/organization-invite.service";
import { InviteMembersRQ } from "../../../invites/model/invite.model";

@Component({
  selector: 'org-invite-members-dialog',
  templateUrl: './org-invite-members-dialog.component.html',
  styleUrl: './org-invite-members-dialog.component.css'
})
export class OrgInviteMembersDialogComponent {
  private organizationId: string;
  public inviteMembersGroup!: FormGroup;

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              private messageService: MessageService,
              private organizationInviteService: OrganizationInviteService) {
    this.organizationId = this.config.data.organizationId;
    this.inviteMembersGroup = new FormGroup({
      inviteMembers: new FormControl([], Validators.compose([
        Validators.maxLength(10)
      ]))
    });
  }

  onInviteMembers() {
    const control = this.control(this.inviteMembersGroup, 'inviteMembers');
    if (!control.valid) {
      return;
    }

    this.organizationInviteService.checkIfExistsInOrganization(this.organizationId, control.value).subscribe({
      next: (isValid) => {
        if (isValid) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Check users selected',
            detail: 'Users you have selected are already invited or part of organization',
          });
        } else {
          this.sendInviteMembers(control);
        }
      }
    })
  }

  private sendInviteMembers(control: FormControl<any>) {
    const rq: InviteMembersRQ = { usernames: control.value }
    this.organizationInviteService.inviteOrganizationMembers(this.organizationId, rq).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Invite created',
          detail: 'Selected users successfully invited into organization',
        });
        this.ref.close(true);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to invite users',
          detail: err.error.message,
        });
      }
    })
  }

  closeDialog() {
    this.ref.close(false);
  }

  protected readonly control = control;
}
