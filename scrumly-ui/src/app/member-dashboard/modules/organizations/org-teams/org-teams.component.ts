import { Component, OnInit } from '@angular/core';
import { OrganizationService } from '../services/organization.service';
import { TeamService } from '../services/team.service';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchFilterService } from '../../../../ui-components/services/search-filter.service';
import { CompareOption, PageDto, SearchOperators } from '../../../../ui-components/models/search-filter.model';
import { OrganizationMemberDto, OrganizationMemberRole, TeamDto, UpdateTeamRQ } from '../model/organization.model';
import { filter } from 'rxjs';
import { DialogService } from 'primeng/dynamicdialog';
import { TeamCreateDialogComponent } from '../dialogs/team-create-dialog/team-create-dialog.component';
import {
  control,
  defaultPageOptions,
  getInviteSeverity,
  specialCharacterValidator
} from '../../../../ui-components/services/utils';
import { PaginatorState } from 'primeng/paginator';
import { ConfirmationService, MessageService } from 'primeng/api';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { OrganizationMemberService } from '../services/organization-member.service';

@Component({
  selector: 'app-org-teams',
  templateUrl: './org-teams.component.html',
  styleUrl: './org-teams.component.css'
})
export class OrgTeamsComponent implements OnInit {
  public teams?: PageDto<TeamDto>;
  public orgId?: string;
  public selectedEditTeam: Map<string, FormControl> = new Map<string, FormControl>();
  public filtersGroup: FormGroup;
  public currentMember?: OrganizationMemberDto;

  constructor(private organizationService: OrganizationService,
              private organizationMemberService: OrganizationMemberService,
              private dialogService: DialogService,
              private teamService: TeamService,
              private activateRoute: ActivatedRoute,
              private router: Router,
              private filterService: SearchFilterService,
              private messageService: MessageService,
              private confirmationService: ConfirmationService) {
    this.filtersGroup = new FormGroup({
      searchField: new FormControl(''),
    });
  }

  ngOnInit(): void {
    this.activateRoute.params.subscribe(params => {
      this.orgId = params['orgId'];
      this.loadTeams();
    });
    this.organizationMemberService.onOrgMemberUpdate
      .pipe(filter(Boolean))
      .subscribe({
        next: (member) => {
          this.currentMember = member!;
        }
      });
  }

  loadTeams() {
    this.teamService.findTeamsByOrganizationId(this.orgId!, this.filterService.searchQuery).subscribe({
      next: (teams) => {
        this.teams = teams;
      }
    })
  }

  openTeamDashboard(team: TeamDto) {
    const selection = this.organizationService.selectedOrganizationGroup.getValue();
    selection!.team = team;
    this.organizationService.updateOrganizationSelection(selection!);
    this.router.navigate([ `/app/teams/${ team.teamId }/dashboard` ]);
  }

  openCreateTeamDialog() {
    const ref = this.dialogService.open(TeamCreateDialogComponent, {
      width: '35vw',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: 'Create new team',
      data: {
        organizationId: this.orgId
      }
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: (team) => {
          if (team) {
            this.teams?.data.unshift(team);
            this.organizationService.onOrganizationEventSubject.next(true);
          }
        }
      })
  }


  onPageChange(page: PaginatorState) {
    this.filterService.changePagination({
      pageSize: page.rows || this.teams?.size!,
      pageIndex: page.page || 0
    });
    this.loadTeams();
  }

  onSearchChange() {
    const value = control(this.filtersGroup, 'searchField').value;
    this.filterService.applySearchFilters([
      {
        operator: SearchOperators.LIKE,
        value: value,
        compareOption: CompareOption.OR,
        property: 'name'
      },
    ]);
    this.loadTeams();
  }


  confirmDeleteTeam(selectedTeam: TeamDto) {
    this.confirmationService.confirm({
      message: `Are you sure that you want to delete: ${ selectedTeam.name } ?`,
      header: 'Delete team',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: 'none',
      rejectIcon: 'none',
      acceptButtonStyleClass: 'p-button-success',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.deleteTeam(selectedTeam);
      }
    });
  }

  deleteTeam(selectedTeam: TeamDto) {
    this.teamService.deleteTeamById(selectedTeam.teamId!).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Team deleted',
          detail: `${ selectedTeam.name } successfully deleted`,
        });
        const idx = this.teams?.data.findIndex(team => team.teamId === selectedTeam.teamId);
        this.teams?.data.splice(idx!, 1);
        this.organizationService.onOrganizationEventSubject.next(true);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to delete team',
          detail: err.error.message,
        });
      },
    })
  }

  onEditTeam(selectedTeam: TeamDto) {
    const control = new FormControl('', Validators.compose([
      specialCharacterValidator, Validators.required, Validators.maxLength(200)
    ]));
    control.setValue(selectedTeam.name!);
    this.selectedEditTeam.set(selectedTeam.teamId!, control);
  }

  onCancelTeamEdit(selectedTeam: TeamDto) {
    this.selectedEditTeam.delete(selectedTeam.teamId!);
  }

  onAcceptTeamEdit(selectedTeam: TeamDto) {
    const control = this.getEditTeamControl(selectedTeam);
    if (!control?.valid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Failed to update team name',
        detail: 'Check form validity',
      });
      return;
    }

    const rq: UpdateTeamRQ = {
      teamName: control.value
    };
    this.teamService.updateTeam(selectedTeam.teamId!, rq).subscribe({
      next: (team) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Team updated',
          detail: `${ team.name } successfully updated`,
        });
        const idx = this.teams?.data.findIndex(value => value.teamId === team.teamId);
        this.teams!.data[idx!] = team;
        this.selectedEditTeam.delete(selectedTeam.teamId!);
        this.organizationService.onOrganizationEventSubject.next(true);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to update team',
          detail: err.error.message,
        });
      }
    })

  }

  getEditTeamControl(selectedTeam: TeamDto): FormControl<string> {
    return this.selectedEditTeam.get(selectedTeam.teamId!)!;
  }


  protected readonly OrganizationMemberRole = OrganizationMemberRole;
  protected readonly defaultPageOptions = defaultPageOptions;
  protected readonly getInviteSeverity = getInviteSeverity;
  protected readonly control = control;
}
