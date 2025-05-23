import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import {
  OrganizationDto,
  OrganizationStatus,
  OrganizationTeamGroupDto,
  TeamDto
} from '../../model/organization.model';
import { OrganizationService } from '../../services/organization.service';
import { filter, Subject, takeUntil } from 'rxjs';
import { TeamService } from '../../services/team.service';

@Component({
  selector: 'org-team-view',
  templateUrl: './org-team-view.component.html',
  styleUrl: './org-team-view.component.css'
})
export class OrgTeamViewComponent implements OnInit, OnChanges, OnDestroy {
  @Input() teamId?: string;
  private destroy$: Subject<void> = new Subject<void>();

  selectedOrg?: OrganizationDto;
  selectedTeam?: TeamDto;
  private orgGroups: OrganizationTeamGroupDto[] = [];

  constructor(private teamService: TeamService) {
  }

  ngOnInit(): void {
    this.loadGroups();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['teamId'] && changes['teamId'].currentValue) {
      this.findSelectedOrgTeam();
    }
  }


  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }


  loadGroups() {
    this.teamService.findOrganizationTeamGroup()
      .pipe(filter(Boolean), takeUntil(this.destroy$))
      .subscribe({
        next: (orgGroups) => {
          this.orgGroups = orgGroups;
          this.findSelectedOrgTeam();
        }
      });
  }

  findSelectedOrgTeam() {
    if (!this.teamId) {
      return;
    }
    this.selectedTeam = this.orgGroups
      .flatMap(group => group.teams)
      .find(temp => temp.teamId === this.teamId);
    this.selectedOrg = this.orgGroups
      .find(group => group.teams
        .find(team => team.teamId! === this.teamId))
      ?.organization;
  }

}
