import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { TeamService } from "../../services/team.service";
import { TeamMemberDto } from "../../model/organization.model";
import { AbstractControl, FormControl } from "@angular/forms";

@Component({
  selector: 'org-team-members-list',
  templateUrl: './org-team-members-list.component.html',
  styleUrl: './org-team-members-list.component.css'
})
export class OrgTeamMembersListComponent implements OnInit, OnChanges {
  @Input() teamId?: string;
  @Input() label: string = "";
  @Input() preSelectedMembersIds: string[] = [];
  @Input() control?: FormControl;
  @Input() excludeList: string[] = [];

  members: TeamMemberDto[] = [];
  targetMembers: TeamMemberDto[] = [];

  teamMembers: TeamMemberDto[] = [];

  constructor(private teamService: TeamService) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['teamId'] && changes['teamId'].currentValue) {
      this.targetMembers = [];
      this.members = [];
      this.control?.setValue("");
      this.loadTeamMembers();
    }
    if (changes['preSelectedMembersIds'] && changes['preSelectedMembersIds'].currentValue) {
      this.loadPreselected();
      this.excludeUsers();
    }
  }

  private loadPreselected() {
    if (this.preSelectedMembersIds.length > 0) {
      this.members = this.teamMembers
        .filter(value => !this.preSelectedMembersIds.includes(value.profile?.userId!));
      this.targetMembers = this.teamMembers
        .filter(value => this.preSelectedMembersIds.includes(value.profile?.userId!));
      this.control?.setValue(this.targetMembers);
    }
  }

  private excludeUsers() {
    if (!this.excludeList) {
      return;
    }
    this.members = this.members
      .filter(value => !this.excludeList.includes(value.profile?.userId!));
    this.targetMembers = this.targetMembers
      .filter(value => !this.excludeList.includes(value.profile?.userId!));
  }

  loadTeamMembers() {
    this.teamService.findTeamById(this.teamId!).subscribe({
      next: (team) => {
        this.members = team.members!;
        this.teamMembers = team.members!;
        this.loadPreselected();
        this.excludeUsers();
      }
    })
  }

  onChanged() {
    this.control?.setValue(this.targetMembers);
    this.control?.markAsDirty();
  }

  get hasError() {
    return this.control?.errors && (this.control.touched || this.control.dirty);
  }

  get isRequired() {
    if (!this.control?.validator) {
      return;
    }
    const validator = this.control?.validator!({} as AbstractControl);
    return validator && validator['required'];
  }
}
