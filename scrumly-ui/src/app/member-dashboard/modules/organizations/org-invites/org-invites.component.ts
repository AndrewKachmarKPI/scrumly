import { Component, OnDestroy, OnInit } from '@angular/core';
import { DialogService } from "primeng/dynamicdialog";
import {
  OrgInviteHistoryDialogComponent
} from "../dialogs/org-invite-history-dialog/org-invite-history-dialog.component";
import {
  OrgInviteMembersDialogComponent
} from "../dialogs/org-invite-members-dialog/org-invite-members-dialog.component";
import { DropdownChangeEvent } from "primeng/dropdown";
import { FormControl, FormGroup } from "@angular/forms";
import { SearchFilterService } from "../../../../ui-components/services/search-filter.service";
import { OrganizationInviteService } from "../services/organization-invite.service";
import { InviteDto, InviteStatus } from "../../invites/model/invite.model";
import { CompareOption, CustomOperators, PageDto } from "../../../../ui-components/models/search-filter.model";
import { MenuItem, MessageService } from "primeng/api";
import { AuthService } from "../../../../auth/auth.service";
import { PaginatorState } from "primeng/paginator";
import { control, defaultPageOptions, getInviteSeverity } from "../../../../ui-components/services/utils";
import { ActivatedRoute, Router } from "@angular/router";
import { InviteService } from "../services/invite.service";
import { OrganizationMemberDto, OrganizationMemberRole } from "../model/organization.model";
import { OrganizationMemberService } from "../services/organization-member.service";
import { filter } from "rxjs";

@Component({
  selector: 'app-org-invites',
  templateUrl: './org-invites.component.html',
  styleUrl: './org-invites.component.css'
})
export class OrgInvitesComponent implements OnInit, OnDestroy {
  public orgId?: string;
  public invites!: PageDto<InviteDto>;
  public filtersGroup: FormGroup;
  public orgMember?: OrganizationMemberDto;

  constructor(private readonly organizationInviteService: OrganizationInviteService,
              private readonly organizationMemberService: OrganizationMemberService,
              public readonly inviteService: InviteService,
              private readonly searchFilterService: SearchFilterService,
              private readonly dialogService: DialogService,
              private readonly authService: AuthService,
              private readonly router: Router,
              private readonly activateRoute: ActivatedRoute,
              private readonly messageService: MessageService) {
    this.filtersGroup = new FormGroup({
      searchField: new FormControl(""),
    });
    this.organizationMemberService.onOrgMemberUpdate
      .pipe(filter(Boolean))
      .subscribe({
        next: (member) => {
          this.orgMember = member;
        }
      });
  }

  ngOnInit(): void {
    this.activateRoute.queryParams.subscribe(params => {
      this.orgId = params['invite'];
      if (this.orgId) {
        this.openInviteMemberDialog();
      }
    });
    this.activateRoute.params.subscribe(params => {
      this.orgId = params['orgId'];
      this.loadInvites();
    });
  }

  ngOnDestroy(): void {
    this.searchFilterService.resetFilterService();
  }


  loadInvites() {
    if (!this.orgId) {
      this.router.navigate(['/app/org/list'])
      return;
    }
    this.organizationInviteService.findOrganizationInvites(this.orgId, this.searchFilterService.searchQuery)
      .subscribe({
        next: (invites) => {
          this.invites = invites;
        }
      })
  }

  onPageChange(page: PaginatorState) {
    this.searchFilterService.changePagination({
      pageSize: page.rows || this.invites.size,
      pageIndex: page.page || 0
    });
    this.loadInvites();
  }

  isCurrentUser(username: string) {
    return this.authService.isCurrentUser(username);
  }

  getInviteActions(invite: InviteDto) {
    let actions: MenuItem[] = [];
    if (invite.currentStatus != InviteStatus.ACCEPTED &&
      invite.currentStatus != InviteStatus.REVOKED) {
      actions.push({
        label: 'Copy invitation',
        icon: 'pi pi-clone',
        command: () => {
          this.copyInvite(invite);
        }
      })
    }
    if (invite.currentStatus === InviteStatus.EXPIRED ||
      invite.currentStatus === InviteStatus.REJECTED) {
      actions.push({
        label: 'Resend',
        icon: 'pi pi-refresh',
        command: () => {
          this.resendInvite(invite);
        }
      });
    }
    if (invite.currentStatus != InviteStatus.REVOKED &&
      invite.currentStatus != InviteStatus.REJECTED &&
      invite.currentStatus != InviteStatus.ACCEPTED) {
      actions.push({
        label: 'Revoke',
        icon: 'pi pi-times-circle',
        command: () => {
          this.revokeInvite(invite);
        }
      });
    }
    if (this.authService.isCurrentUser(invite.createBy?.userId!) &&
      invite.currentStatus !== InviteStatus.ACCEPTED) {
      actions.push({
        label: 'Delete',
        icon: 'pi pi-trash',
        iconClass: 'text-red-500',
        command: () => {
          this.deleteInvite(invite);
        }
      });
    }
    return actions;
  }


  copyInvite(invite: InviteDto) {
    this.inviteService.copyInvite(invite);
  }

  resendInvite(invite: InviteDto) {
    this.organizationInviteService.resendInvite(this.orgId!, invite.inviteId!).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Invite success',
          detail: 'Your invite successfully resent!',
        });
        this.loadInvites();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Invite error',
          detail: 'Failed to resent your invite!',
        });
      }
    })
  }

  revokeInvite(invite: InviteDto) {
    this.organizationInviteService.revokeInvite(this.orgId!, invite.inviteId!).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Invite success',
          detail: 'Your invite successfully revoked!',
        });
        this.loadInvites();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Invite error',
          detail: 'Failed to revoke your invite!',
        });
      }
    })
  }

  deleteInvite(invite: InviteDto) {
    this.organizationInviteService.deleteInvite(this.orgId!, invite.inviteId!).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Invite success',
          detail: 'Your invite successfully deleted!',
        });
        this.loadInvites();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Invite error',
          detail: 'Failed to delete your invite!',
        });
      }
    })
  }

  onStatusChange(event: DropdownChangeEvent) {
    const key = 'currentStatus';
    if (event.value) {
      this.searchFilterService.applySearchFilter({
        value: event.value,
        compareOption: CompareOption.AND,
        operator: CustomOperators.IS_INVITE_STATUS_EQUAL,
        property: key
      });
    } else {
      this.searchFilterService.resetSearchFilter(key)
    }
    this.loadInvites();
  }

  openInviteMemberDialog() {
    const ref = this.dialogService.open(OrgInviteMembersDialogComponent, {
      width: '45vw',
      breakpoints: {
        '1199px': '75vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: 'Invite new members',
      data: {
        organizationId: this.orgId!
      }
    });
    ref.onClose.subscribe({
      next: (res) => {
        if (res) {
          this.loadInvites();
        }
      }
    })
  }

  openInviteHistoryDialog(invite: InviteDto) {
    this.dialogService.open(OrgInviteHistoryDialogComponent, {
      width: '25vw',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: 'Invite history',
      data: {
        invite: invite
      }
    })
  }

  protected readonly defaultPageOptions = defaultPageOptions;
  protected readonly getInviteSeverity = getInviteSeverity;
  protected readonly control = control;
  protected readonly OrganizationMemberRole = OrganizationMemberRole;
}
