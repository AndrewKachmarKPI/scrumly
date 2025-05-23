import { Component, Input, OnInit } from '@angular/core';
import {
  OrganizationMemberDto,
  OrganizationMemberRole,
  TeamDto,
  TeamMemberDto,
  TeamMemberRole,
  UpdateTeamRQ
} from '../../model/organization.model';
import {
  control,
  defaultPageOptions,
  getMemberRoleSeverity,
  getTeamMemberRoleSeverity, specialCharacterValidator
} from '../../../../../ui-components/services/utils';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { TeamService } from '../../services/team.service';
import {
  OrgMemberHistoryDialogComponent
} from '../../dialogs/org-member-history-dialog/org-member-history-dialog.component';
import { DialogService } from 'primeng/dynamicdialog';
import { AccordionTab } from 'primeng/accordion';
import {
  TeamInviteMembersDialogComponent
} from '../../dialogs/team-invite-members-dialog/team-invite-members-dialog.component';
import { filter } from 'rxjs';
import { AuthService } from '../../../../../auth/auth.service';

@Component({
  selector: 'org-team-members-table',
  templateUrl: './org-team-members-table.component.html',
  styleUrl: './org-team-members-table.component.css'
})
export class OrgTeamMembersTableComponent implements OnInit {
  @Input() team?: TeamDto;
  @Input() showCaption: boolean = false;
  @Input() organizationMember?: OrganizationMemberDto;

  public currentMember?: TeamMemberDto;

  public cols: any[] = [];
  public selectedEditMember: Map<string, FormGroup> = new Map<string, FormGroup>();


  constructor(private translateService: TranslateService,
              private teamService: TeamService,
              private messageService: MessageService,
              private dialogService: DialogService,
              private authService: AuthService,
              private confirmationService: ConfirmationService) {
    this.cols = [
      { field: 'profile.firstName', customExportHeader: 'First name' },
      { field: 'profile.lastName', customExportHeader: 'Last name' },
      { field: 'profile.email', customExportHeader: 'Email' },
      { field: 'badge', customExportHeader: 'Badge' },
      { field: 'role', customExportHeader: 'Role' },
    ];
  }

  ngOnInit(): void {
    const userId = this.authService.getUserId();
    if (this.team) {
      this.currentMember = this.team?.members?.find(member => member?.profile?.userId === userId);
    }
  }

  openTeamMemberHistory(member: TeamMemberDto) {
    this.dialogService.open(OrgMemberHistoryDialogComponent, {
      width: '25vw',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: `${ member.profile?.firstName } ${ member.profile?.lastName } history`,
      data: {
        changeHistory: member.changeHistory
      }
    })
  }

  onAcceptMemberEdit(member: TeamMemberDto) {
    const group = this.selectedEditMember.get(member.profile?.userId!);
    if (!group?.valid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Failed to update team member',
        detail: 'Check form validity',
      });
      return;
    }

    const rq: UpdateTeamRQ = {
      updateMembers: [
        {
          userId: member.profile?.userId!,
          badge: control(group, 'badge').value,
          memberRole: control(group, 'role').value
        }
      ]
    };

    this.teamService.updateTeam(this.team?.teamId!, rq).subscribe({
      next: (updatedTeam) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Team member updated',
          detail: `${ member.profile?.firstName } ${ member.profile?.lastName } successfully updated`,
        });
        this.team = updatedTeam;
        this.selectedEditMember.delete(member.profile?.userId!);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to update team member',
          detail: err.error.message,
        });
      }
    })

  }

  onCancelMemberEdit(member: TeamMemberDto) {
    this.selectedEditMember.delete(member.profile?.userId!);
  }

  onEditTeamMember(member: TeamMemberDto) {
    this.selectedEditMember.set(member.profile?.userId!, new FormGroup({
      badge: new FormControl(member.badge, Validators.compose([
        specialCharacterValidator, Validators.maxLength(100)
      ])),
      role: new FormControl(member.role, Validators.compose([
        Validators.required
      ]))
    }));
  }

  getEditTeamMemberControl(member: TeamMemberDto, name: string) {
    const group = this.selectedEditMember.get(member.profile?.userId!);
    return control(group!, name);
  }

  getTeamMemberRoleOptions(member: TeamMemberDto) {
    const values: string[] = Object.values(TeamMemberRole);
    const options = values.map(value => {
      return {
        value: value,
        name: this.translateService.instant('organizations.members.TEAM_MEMBER_ROLE.' + value)
      }
    });
    return options.filter(option => option.value != TeamMemberRole.TEAM_ADMIN);
  }


  onDeleteMember(member: TeamMemberDto) {
    this.confirmationService.confirm({
      message: `Are you sure that you want to delete: ${ member.profile?.firstName } ${ member.profile?.lastName } from team ?`,
      header: 'Delete team member',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: 'none',
      rejectIcon: 'none',
      acceptButtonStyleClass: 'p-button-success',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.deleteTeamMember(member);
      }
    });
  }

  deleteTeamMember(member: TeamMemberDto) {
    this.teamService.removeTeamMembers(this.team?.teamId!, [ member.profile?.userId! ]).subscribe({
      next: (updatedTeam) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Team member deleted',
          detail: `${ member.profile?.firstName } ${ member.profile?.lastName } successfully deleted`,
        });
        this.team = updatedTeam;
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to delete team member',
          detail: err.error.message,
        });
      }
    })
  }

  openInviteTeamMemberDialog() {
    const memberIds = this.team?.members!.map(value => value.profile?.userId);
    const ref = this.dialogService.open(TeamInviteMembersDialogComponent, {
      width: '35vw',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: `Add team members`,
      data: {
        orgId: this.team?.organizationId,
        teamId: this.team?.teamId,
        excludeIds: memberIds
      }
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: (team) => {
          if (team) {
            this.team = team;
          }
        }
      })
  }

  protected readonly defaultPageOptions = defaultPageOptions;
  protected readonly OrganizationMemberRole = OrganizationMemberRole;
  protected readonly getTeamMemberRoleSeverity = getTeamMemberRoleSeverity;
  protected readonly TeamMemberRole = TeamMemberRole;
  protected readonly customElements = customElements;
}
