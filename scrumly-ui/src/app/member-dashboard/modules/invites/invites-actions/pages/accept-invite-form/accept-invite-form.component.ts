import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { InviteDto, InviteType } from "../../../model/invite.model";
import { joinBy } from "../../../../../../ui-components/services/utils";
import { MessageService } from "primeng/api";

@Component({
  selector: 'app-accept-invite-form',
  templateUrl: './accept-invite-form.component.html',
  styleUrl: './accept-invite-form.component.css'
})
export class AcceptInviteFormComponent implements OnInit {
  public invite?: InviteDto;
  private inviteId: string | null;

  constructor(private readonly route: ActivatedRoute,
              private readonly router: Router,
              private messageService: MessageService) {
    this.inviteId = this.route.snapshot.paramMap.get('inviteId');
  }

  ngOnInit(): void {
    this.loadInvite();
  }


  loadInvite() {
    if (!this.inviteId) {
      this.router.navigate(['']);
      return;
    }
    // this.inviteService.findInvite(this.inviteId).subscribe({
    //   next: (invite) => {
    //     this.invite = invite;
    //   },
    //   error: (err) => {
    //     this.messageService.add({
    //       severity: 'error',
    //       summary: 'Invite error',
    //       detail: err.error.message,
    //     });
    //     this.router.navigate(['']);
    //   }
    // })
  }

  rejectInvitation() {
    // this.inviteService.rejectInvite(this.inviteId!).subscribe({
    //   next: () => {
    //     this.messageService.add({
    //       severity: 'success',
    //       summary: 'Invite success',
    //       detail: 'Invite successfully rejected'
    //     });
    //     this.router.navigate([''])
    //   },
    //   error: (err) => {
    //     this.messageService.add({
    //       severity: 'error',
    //       summary: 'Invite error',
    //       detail: err.error.message,
    //     });
    //   }
    // })
  }
  acceptInvitation() {
    // this.inviteService.acceptInvite(this.inviteId!).subscribe({
    //   next: () => {
    //     this.messageService.add({
    //       severity: 'success',
    //       summary: 'Invite success',
    //       detail: 'Invite successfully accepted'
    //     });
    //     if (this.invite?.inviteType === InviteType.ORGANIZATION) {
    //       this.router.navigate(['/org/dashboard'])
    //     }
    //   },
    //   error: (err) => {
    //     this.messageService.add({
    //       severity: 'error',
    //       summary: 'Invite error',
    //       detail: err.error.message,
    //     });
    //   }
    // })
  }

  protected readonly joinBy = joinBy;
}
