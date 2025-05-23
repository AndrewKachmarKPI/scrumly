import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from "@angular/router";
import { SearchFilterService } from "../../../../ui-components/services/search-filter.service";
import { MemberStatus, OrganizationMemberDto, OrganizationMemberRole } from "../model/organization.model";
import {
  CompareOption,
  CustomOperators,
  PageDto,
  SearchOperators
} from "../../../../ui-components/models/search-filter.model";
import { control, defaultPageOptions, getMemberRoleSeverity, joinBy } from "../../../../ui-components/services/utils";
import { PaginatorState } from "primeng/paginator";
import { MenuItem, MessageService } from "primeng/api";
import { FormControl, FormGroup } from "@angular/forms";
import { DropdownChangeEvent } from "primeng/dropdown";
import { AuthService } from "../../../../auth/auth.service";
import { OrganizationMemberService } from "../services/organization-member.service";
import {
  OrgMemberHistoryDialogComponent
} from "../dialogs/org-member-history-dialog/org-member-history-dialog.component";
import { DialogService } from "primeng/dynamicdialog";
import { filter } from "rxjs";

@Component({
  selector: 'app-org-members',
  templateUrl: './org-members.component.html',
  styleUrl: './org-members.component.css'
})
export class OrgMembersComponent implements OnInit, OnDestroy {
  public orgId?: string;
  public members!: PageDto<OrganizationMemberDto>;
  public currentMember?: OrganizationMemberDto;
  public filtersGroup: FormGroup;
  public cols: any[] = [];

  constructor(public readonly organizationMemberService: OrganizationMemberService,
              private readonly searchFilterService: SearchFilterService,
              private readonly authService: AuthService,
              private readonly messageService: MessageService,
              private readonly activateRoute: ActivatedRoute,
              private dialogService: DialogService) {
    this.filtersGroup = new FormGroup({
      searchField: new FormControl(""),
    });
    this.cols = [
      { field: 'profile.firstName', customExportHeader: 'First name' },
      { field: 'profile.lastName', customExportHeader: 'Last name' },
      { field: 'profile.email', customExportHeader: 'Email' },
      { field: 'role', customExportHeader: 'Role' },
      { field: 'status', customExportHeader: 'Status' },
    ];

    this.organizationMemberService.onOrgMemberUpdate
      .pipe(filter(Boolean))
      .subscribe({
        next: (member) => {
          this.currentMember = member!;
        }
      });
  }

  ngOnInit(): void {
    this.activateRoute.params.subscribe(params => {
      this.orgId = params['orgId'];
      this.loadMembers();
    });
  }

  ngOnDestroy(): void {
    this.searchFilterService.resetFilterService();
  }


  loadMembers() {
    this.organizationMemberService.findOrganizationMembers(this.orgId!, this.searchFilterService.searchQuery)
      .subscribe({
        next: (membersPage) => {
          this.members = membersPage;
        }
      })
  }

  onPageChange(page: PaginatorState) {
    this.searchFilterService.changePagination({
      pageSize: page.rows || this.members.size,
      pageIndex: page.page || 0
    });
    this.loadMembers();
  }

  onSearchChange() {
    const value = control(this.filtersGroup, 'searchField').value;
    this.searchFilterService.applySearchFilters([
      {
        operator: SearchOperators.LIKE,
        value: value,
        compareOption: CompareOption.OR,
        property: 'profile.firstName'
      },
      {
        operator: SearchOperators.LIKE,
        value: value,
        compareOption: CompareOption.OR,
        property: 'profile.lastName'
      },
      {
        operator: SearchOperators.LIKE,
        value: value,
        compareOption: CompareOption.OR,
        property: 'profile.email'
      }
    ]);
    this.loadMembers();
  }

  onMemberRoleChange(event: DropdownChangeEvent) {
    const key = 'role';
    if (event.value) {
      this.searchFilterService.applySearchFilter({
        value: event.value,
        compareOption: CompareOption.AND,
        operator: CustomOperators.IS_MEMBER_ROLE_EQUAL,
        property: key
      });
    } else {
      this.searchFilterService.resetSearchFilter(key)
    }
    this.loadMembers();
  }

  canModifyMember(member: OrganizationMemberDto) {
    return !this.authService.isCurrentUser(member.profile?.userId!) &&
      member.role != OrganizationMemberRole.ORGANIZATION_ADMIN;
  }

  getMemberActions(member: OrganizationMemberDto) {
    let actions: MenuItem[] = [];
    if (member.role === OrganizationMemberRole.MEMBER && member.status == MemberStatus.ACTIVE) {
      actions.push({
        label: 'Promote to Organization Lead',
        icon: 'pi pi-user-plus',
        command: () => {
          this.promoteToOrganizationLead(member);
        }
      });
    }
    if (member.role === OrganizationMemberRole.ORGANIZATION_LEAD) {
      actions.push({
        label: 'Demote to Organization Member',
        icon: 'pi pi-user-minus',
        command: () => {
          this.demoteToOrganizationMember(member);
        }
      });
    }
    if (member.role != OrganizationMemberRole.ORGANIZATION_ADMIN) {
      if (member.status == MemberStatus.ACTIVE) {
        actions.push({
          label: 'Block member',
          icon: 'pi pi-pause-circle',
          iconClass: 'text-yellow-500',
          command: () => {
            this.blockOrganizationMembership(member);
          }
        });
      }
      if (member.status == MemberStatus.BLOCKED) {
        actions.push({
          label: 'Activate member',
          icon: 'pi pi-play-circle',
          iconClass: 'text-success-500',
          command: () => {
            this.activateOrganizationMembership(member);
          }
        });
      }
      actions.push({
        label: 'Remove from organization',
        icon: 'pi pi-trash',
        iconClass: 'text-red-500',
        command: () => {
          this.removeFromOrganization(member);
        }
      });
    }
    return actions;
  }


  promoteToOrganizationLead(member: OrganizationMemberDto) {
    this.organizationMemberService.changeMemberRole(this.orgId!, member.profile?.userId!, OrganizationMemberRole.ORGANIZATION_LEAD).subscribe({
      next: (updatedMember) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Role Updated',
          detail: `${ member.profile?.firstName } ${ member.profile?.lastName } has been promoted to Organization Lead.`,
        });
        this.updateMemberRow(updatedMember);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to Promote Member',
          detail: err.error.message,
        });
      },
    });
  }

  demoteToOrganizationMember(member: OrganizationMemberDto) {
    this.organizationMemberService.changeMemberRole(this.orgId!, member.profile?.userId!, OrganizationMemberRole.MEMBER).subscribe({
      next: (updatedMember) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Role Updated',
          detail: `${ member.profile?.firstName } ${ member.profile?.lastName } has been demoted to Organization Member.`,
        });
        this.updateMemberRow(updatedMember);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to Demote Member',
          detail: err.error.message,
        });
      },
    });
  }

  blockOrganizationMembership(member: OrganizationMemberDto) {
    this.organizationMemberService.blockMember(this.orgId!, member.profile?.userId!).subscribe({
      next: (updatedMember) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Membership Suspended',
          detail: `${ member.profile?.firstName } ${ member.profile?.lastName }'s membership has been suspended.`,
        });
        this.updateMemberRow(updatedMember);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to Suspend Membership',
          detail: err.error.message,
        });
      },
    });
  }

  activateOrganizationMembership(member: OrganizationMemberDto) {
    this.organizationMemberService.activateMember(this.orgId!, member.profile?.userId!).subscribe({
      next: (updatedMember) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Membership Activated',
          detail: `${ member.profile?.firstName } ${ member.profile?.lastName }'s membership has been activated.`,
        });
        this.updateMemberRow(updatedMember);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to Activate Membership',
          detail: err.error.message,
        });
      },
    });
  }


  removeFromOrganization(member: OrganizationMemberDto) {
    this.organizationMemberService.removeMember(this.orgId!, member.profile?.userId!).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Member Removed',
          detail: `${ member.profile?.firstName } ${ member.profile?.lastName } has been removed from the organization.`,
        });
        this.loadMembers();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to Remove Member',
          detail: err.error.message,
        });
      },
    });
  }


  openMemberHistory(member: OrganizationMemberDto) {
    this.dialogService.open(OrgMemberHistoryDialogComponent, {
      width: '25vw',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: 'Invite history',
      data: {
        changeHistory: member.changeHistory
      }
    })
  }

  updateMemberRow(member: OrganizationMemberDto) {
    const dataMember = this.members.data.find(mem => mem.profile?.userId === member.profile?.userId);
    if (dataMember) {
      const idx = this.members.data.indexOf(dataMember);
      this.members.data[idx] = member;
    }
  }

  protected readonly defaultPageOptions = defaultPageOptions;
  protected readonly getMemberRoleSeverity = getMemberRoleSeverity;
  protected readonly joinBy = joinBy;
  protected readonly control = control;
  protected readonly OrganizationMemberRole = OrganizationMemberRole;
}
