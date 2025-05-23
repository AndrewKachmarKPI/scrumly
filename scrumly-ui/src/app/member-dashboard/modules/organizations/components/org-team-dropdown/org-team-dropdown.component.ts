import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import { SelectItemGroup } from 'primeng/api';
import { AbstractControl, FormControl } from '@angular/forms';
import { TeamService } from '../../services/team.service';
import { OrganizationDto, OrganizationStatus, OrganizationTeamGroupDto, TeamDto } from '../../model/organization.model';
import { Dropdown, DropdownChangeEvent } from 'primeng/dropdown';
import { OrganizationGroupSelection, OrganizationService } from '../../services/organization.service';
import { filter, Subscription } from 'rxjs';

@Component({
  selector: 'org-team-dropdown',
  templateUrl: './org-team-dropdown.component.html',
  styleUrl: './org-team-dropdown.component.css'
})
export class OrgTeamDropdownComponent implements OnInit, OnChanges, OnDestroy {
  @Input() id: string = '';
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() styleClass: string = '';
  @Input() disabled: boolean = false;
  @Input() readonly: boolean = false;
  @Input() showClear: boolean = false;
  @Input() showOrganization: boolean = false;
  @Input() selectedTeamId?: string;
  @Input() control = new FormControl('');
  @Output() onSelect: EventEmitter<OrganizationGroupSelection> = new EventEmitter<OrganizationGroupSelection>();

  groupedOrganizations: SelectItemGroup[] = [];
  groups: OrganizationTeamGroupDto[] = [];

  selectedOrganization?: OrganizationDto;
  selectedTeam?: TeamDto;

  subscription: Subscription = new Subscription();

  constructor(private teamService: TeamService,
              private organizationService: OrganizationService) {
  }

  ngOnInit(): void {
    this.loadGroups();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['disabled']) {
      this.toggleControl();
    }
    if (changes['selectedTeamId'] && changes['selectedTeamId'].currentValue) {
      this.onSelectTeam(this.selectedTeamId!);
    }
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  loadGroups() {
    const sub = this.organizationService.organizationTeamGroupSub.subscribe({
      next: (organization) => {
        this.groups = organization;
        this.groupedOrganizations = organization.map(group => {
          return {
            label: group.organization.name!,
            value: group.organization,
            items: group.teams.map(team => {
              return {
                label: team.name,
                value: team.teamId,
                disabled: group.organization.status != OrganizationStatus.ACTIVE
              }
            })
          };
        });
        if (this.selectedTeamId) {
          this.onSelectTeam(this.selectedTeamId);
        }
      }
    });
    this.subscription.add(sub);
  }

  onSelectTeamChange(event: DropdownChangeEvent) {
    this.onSelectTeam(event.value);
    this.onSelect.emit({
      org: this.selectedOrganization,
      team: this.selectedTeam
    })
  }

  private onSelectTeam(teamId: string) {
    this.selectedTeam = this.groups
      .flatMap(group => group.teams)
      .find(temp => temp.teamId === teamId);
    this.selectedOrganization = this.groups
      .find(group => group.teams
        .find(team => team.teamId! === teamId))
      ?.organization;
    this.control.setValue(this.selectedTeam?.teamId!);
  }

  toggleControl() {
    if (this.disabled) {
      this.control.disable();
    } else {
      this.control.enable();
    }
  }

  get hasError() {
    return this.control.errors && (this.control.touched || this.control.dirty);
  }

  get isRequired() {
    if (!this.control.validator) {
      return;
    }
    const validator = this.control.validator!({} as AbstractControl);
    return validator && validator['required'];
  }
}
