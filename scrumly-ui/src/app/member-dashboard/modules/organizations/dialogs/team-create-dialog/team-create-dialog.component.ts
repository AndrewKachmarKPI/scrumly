import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { control, lineEmailValidator, specialCharacterValidator } from "../../../../../ui-components/services/utils";
import { TeamService } from "../../services/team.service";
import { CreateTeamRQ, OrganizationInfoDto } from "../../model/organization.model";
import { MessageService } from "primeng/api";
import { OrganizationMemberService } from "../../services/organization-member.service";
import { SearchFilterService } from "../../../../../ui-components/services/search-filter.service";
import { UserProfileDto } from "../../../../../auth/auth.model";

@Component({
  selector: 'app-team-create-dialog',
  templateUrl: './team-create-dialog.component.html',
  styleUrl: './team-create-dialog.component.css'
})
export class TeamCreateDialogComponent implements OnInit {
  public organizationMembers: UserProfileDto[] = [];
  public organizationId?: string;
  public createTeamGroup!: FormGroup;

  constructor(public ref: DynamicDialogRef,
              public config: DynamicDialogConfig,
              private organizationMemberService: OrganizationMemberService,
              private filterService: SearchFilterService,
              private messageService: MessageService,
              private teamService: TeamService) {
    this.createTeamGroup = new FormGroup({
      teamName: new FormControl('', Validators.compose([
        specialCharacterValidator, Validators.required, Validators.maxLength(200)
      ])),
      inviteMembers: new FormControl([], Validators.compose([
        Validators.maxLength(10), Validators.required, Validators.minLength(1)
      ]))
    })
  }

  ngOnInit(): void {
    this.organizationId = this.config.data.organizationId;
    this.loadOrganizationMembers();
  }

  loadOrganizationMembers() {
    this.filterService.changePagination({
      pageSize: 1000,
      pageIndex: 0
    });
    this.organizationMemberService.findOrganizationMembers(this.organizationId!, this.filterService.searchQuery)
      .subscribe({
        next: (membersPage) => {
          this.organizationMembers = membersPage.data.map(member => member.profile!);
        }
      });
  }

  createTeam() {
    if (!this.createTeamGroup.valid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Invalid form',
        detail: 'Check form validity',
      })
      return;
    }
    const rq: CreateTeamRQ = {
      organizationId: this.organizationId!,
      teamName: control(this.createTeamGroup, 'teamName').value,
      inviteMembers: control(this.createTeamGroup, 'inviteMembers').value
    };

    this.teamService.createTeam(rq).subscribe({
      next: (team) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Team created',
          detail: 'You have successfully created new team',
        });
        this.ref.close(team);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to create team',
          detail: 'Error while creating team check form validity',
        })
      }
    })
  }


  closeDialog() {
    this.ref.close();
  }

  protected readonly control = control;
}
