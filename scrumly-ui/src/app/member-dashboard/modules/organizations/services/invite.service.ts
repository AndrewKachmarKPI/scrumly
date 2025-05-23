import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../../../../enviroments/enviroment";
import { MessageService } from "primeng/api";
import { Clipboard } from "@angular/cdk/clipboard";
import { TranslateService } from "@ngx-translate/core";
import { InviteDto, InviteStatus } from "../../invites/model/invite.model";

@Injectable({
  providedIn: 'root'
})
export class InviteService {

  constructor(private httpClient: HttpClient,
              private clipboard: Clipboard,
              private translateService: TranslateService,
              private messageService: MessageService) {
  }

  copyInvite(invite: InviteDto) {
    this.clipboard.copy(invite.inviteUrl!);
    this.messageService.add({
      severity: 'info',
      summary: 'Copy to clipboard',
      detail: 'Member invitation link copied',
    });
  }

  getInviteStatusOptions() {
    const values: string[] = Object.values(InviteStatus);
    return values.map(value => {
      return {
        value: value,
        name: this.translateService.instant('organizations.members.INVITE_STATUS.' + value)
      }
    })
  }

  public findMyPendingInvites(): Observable<InviteDto[]> {
    const url = `${ environment.api_url }/users/invites/me`
    return this.httpClient.get<InviteDto[]>(url);
  }

}
