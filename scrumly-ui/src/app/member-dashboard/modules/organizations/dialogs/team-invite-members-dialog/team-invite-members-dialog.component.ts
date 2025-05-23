import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { MessageService } from "primeng/api";
import { OrganizationInviteService } from "../../services/organization-invite.service";
import { InviteMembersRQ } from "../../../invites/model/invite.model";
import { TeamService } from "../../services/team.service";
import { control } from "../../../../../ui-components/services/utils";

@Component({
  selector: 'app-team-invite-members-dialog',
  templateUrl: './team-invite-members-dialog.component.html',
  styleUrl: './team-invite-members-dialog.component.css'
})
export class TeamInviteMembersDialogComponent {
  public orgId: string;
  private teamId: string;
  public excludeIds: string[] = [];
  public inviteMembersGroup!: FormGroup;

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              private messageService: MessageService,
              private teamService: TeamService) {
    this.orgId = this.config.data.orgId;
    this.teamId = this.config.data.teamId;
    this.excludeIds = this.config.data.excludeIds;
    this.inviteMembersGroup = new FormGroup({
      inviteMembers: new FormControl([], Validators.compose([
        Validators.maxLength(10)
      ]))
    });
  }

  onInviteMembers() {
    const membersControl = control(this.inviteMembersGroup, 'inviteMembers');
    if (!membersControl.valid) {
      return;
    }
    this.teamService.inviteTeamMembers(this.teamId, membersControl.value).subscribe({
      next: (team) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Members invites into team',
          detail: 'Selected users successfully invited into team',
        });
        this.ref.close(team);
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
