import { Component, OnInit } from '@angular/core';
import { OrganizationInviteService } from "../../../organizations/services/organization-invite.service";
import { InviteDto, InviteType } from "../../../invites/model/invite.model";
import { SearchFilterService } from "../../../../../ui-components/services/search-filter.service";
import { FormControl, FormGroup } from "@angular/forms";
import { control, defaultPageOptions } from "../../../../../ui-components/services/utils";
import { OrganizationService } from "../../../organizations/services/organization.service";
import { ConfirmationService, MessageService } from "primeng/api";
import { Router } from "@angular/router";


@Component({
  selector: 'app-member-invites',
  templateUrl: './member-invites.component.html',
  styleUrl: './member-invites.component.css'
})
export class MemberInvitesComponent implements OnInit {
  public invites: InviteDto[] = [];
  public filtersGroup: FormGroup;
  inviteTypeOptions: any[] = [
    {
      label: 'All',
      value: InviteType.ALL
    },
    {
      label: 'Organization',
      value: InviteType.ORGANIZATION
    },
    {
      label: 'Team',
      value: InviteType.TEAM
    }
  ];

  constructor(private readonly organizationInviteService: OrganizationInviteService,
              private readonly organizationService: OrganizationService,
              private readonly searchService: SearchFilterService,
              private readonly messageService: MessageService,
              private confirmationService: ConfirmationService,
              private readonly router: Router) {
    this.filtersGroup = new FormGroup({
      inviteType: new FormControl(InviteType.ALL),
    })
  }

  ngOnInit(): void {
    this.searchService.changePagination({
      pageSize: 1000,
      pageIndex: 0
    });
    this.loadOrganizationInvites();
  }

  private loadOrganizationInvites() {
    this.organizationInviteService.findMyOrganizationInvites(this.searchService.searchQuery)
      .subscribe({
        next: (invites) => {
          this.invites = invites.data;
        }
      });
  }

  onInviteTypeSelect() {
    this.filterInvites();
  }

  filterInvites() {
    const inviteType = this.control(this.filtersGroup, 'inviteType').value;
    if (inviteType === InviteType.ALL) {
      this.loadOrganizationInvites();
    } else if (inviteType === InviteType.ORGANIZATION) {
      this.loadOrganizationInvites();
    } else if (inviteType === InviteType.TEAM) {

    }
  }


  acceptInvite(invite: InviteDto, event: Event) {
    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: 'Are you sure you want accept invite?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        if (invite.inviteType === InviteType.ORGANIZATION) {
          this.acceptOrgInvite(invite);
        }
      }
    });
  }

  rejectInvite(invite: InviteDto, event: Event) {
    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: 'Are you sure you want reject invite?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        if (invite.inviteType === InviteType.ORGANIZATION) {
          this.rejectOrgInvite(invite);
        }
      }
    });
  }

  acceptOrgInvite(invite: InviteDto) {
    const orgId = invite.orgInfoDto?.organizationId;
    this.organizationInviteService.acceptInvite(orgId!, invite.inviteId!).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Invite success',
          detail: 'Invite successfully accepted'
        });
        this.organizationService.updateOrganizationSelection({
          org: invite.orgInfoDto!
        });
        this.organizationService.onOrganizationEventSubject.next(true);
        this.router.navigate([`/app/org/${ orgId }/dashboard`]);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Invite error',
          detail: err.error.message || 'Internal error',
        });
      }
    })
  }

  rejectOrgInvite(invite: InviteDto) {
    const orgId = invite.orgInfoDto?.organizationId;
    this.organizationInviteService.rejectInvite(orgId!, invite.inviteId!).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Invite success',
          detail: 'Invite successfully rejected'
        });
        this.filterInvites();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Invite error',
          detail: err.error.message || 'Internal error',
        });
      }
    })
  }

  protected readonly control = control;
  protected readonly InviteType = InviteType;
  protected readonly defaultPageOptions = defaultPageOptions;
}
