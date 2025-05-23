import { Component, OnDestroy, OnInit } from '@angular/core';
import { TeamService } from '../../../organizations/services/team.service';
import { SearchFilterService } from '../../../../../ui-components/services/search-filter.service';
import {
  OrganizationMemberRole,
  OrganizationTeamGroupDto,
  TeamDto
} from '../../../organizations/model/organization.model';
import { control, defaultPageOptions } from '../../../../../ui-components/services/utils';
import { Subject, takeUntil } from 'rxjs';
import { OrganizationGroupSelection, OrganizationService } from '../../../organizations/services/organization.service';
import { PaginatorState } from 'primeng/paginator';

@Component({
  selector: 'app-my-teams',
  templateUrl: './my-teams.component.html',
  styleUrl: './my-teams.component.css'
})
export class MyTeamsComponent implements OnInit, OnDestroy {

  selection?: OrganizationGroupSelection;
  orgTeamGroup?: OrganizationTeamGroupDto[];

  private destroy$: Subject<void> = new Subject();

  constructor(private teamService: TeamService,
              private organizationService: OrganizationService,
              private filterService: SearchFilterService) {
  }

  ngOnInit(): void {
    this.loadMyTeams();
    this.organizationService.onOrganizationSelectionChange
      .pipe(takeUntil(this.destroy$))
      .subscribe(org => {
        this.selection = org;
      });
  }


  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.filterService.resetFilterService();
  }


  loadMyTeams() {
    this.teamService.findOrganizationTeamGroup()
      .subscribe({
        next: (orgTeamGroup) => {
          this.orgTeamGroup = orgTeamGroup;
        }
      })
  }


  onSelectTeam(group: OrganizationGroupSelection, team: TeamDto) {
    const selectionJson = {
      org: group.org!,
      team: team
    };
    localStorage.setItem('organizationGroupSelection', JSON.stringify(selectionJson));
    this.organizationService.updateOrganizationSelection(selectionJson);
  }

  onPageChange(page: PaginatorState) {
    this.filterService.changePagination({
      pageSize: page.rows!,
      pageIndex: page.page!
    });
    this.loadMyTeams();
  }


  protected readonly control = control;
  protected readonly defaultPageOptions = defaultPageOptions;
  protected readonly OrganizationMemberRole = OrganizationMemberRole;
}
