import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { PageDto, SearchQuery } from "../../../../ui-components/models/search-filter.model";
import { Observable } from "rxjs";
import { environment } from "../../../../../enviroments/enviroment";
import { OrganizationService } from "./organization.service";
import { InviteDto, InviteMembersRQ } from "../../invites/model/invite.model";

@Injectable({
  providedIn: 'root'
})
export class OrganizationInviteService {

  constructor(private httpClient: HttpClient,
              private organizationService: OrganizationService) {
  }

  public findOrganizationInvites(orgId: string, searchQuery: SearchQuery): Observable<PageDto<InviteDto>> {
    const url = `${ environment.api_url }/users/org/${ orgId }/invites/all`
    return this.httpClient.post<PageDto<InviteDto>>(url, searchQuery);
  }

  public findMyOrganizationInvites(searchQuery: SearchQuery): Observable<PageDto<InviteDto>> {
    const url = `${ environment.api_url }/users/invites/org/me`
    return this.httpClient.post<PageDto<InviteDto>>(url, searchQuery);
  }

  public inviteOrganizationMembers(orgId: string, rq: InviteMembersRQ) {
    const url = `${ environment.api_url }/users/org/${ orgId }/invites/invite`
    return this.httpClient.put<PageDto<InviteDto>>(url, rq);
  }

  public findInvite(orgId: string, inviteId: string): Observable<InviteDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }/invites/${inviteId}`
    return this.httpClient.get<InviteDto>(url);
  }

  public resendInvite(orgId: string, inviteId: string): Observable<InviteDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }/invites/${inviteId}/resend`
    return this.httpClient.post<InviteDto>(url, {});
  }

  public acceptInvite(orgId: string, inviteId: string): Observable<InviteDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }/invites/${inviteId}/accept`
    return this.httpClient.post<InviteDto>(url, {});
  }

  public rejectInvite(orgId: string, inviteId: string): Observable<InviteDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }/invites/${inviteId}/reject`
    return this.httpClient.post<InviteDto>(url, {});
  }

  public revokeInvite(orgId: string, inviteId: string): Observable<InviteDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }/invites/${inviteId}/revoke`
    return this.httpClient.post<InviteDto>(url, {});
  }

  public deleteInvite(orgId: string, inviteId: string): Observable<InviteDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }/invites/${inviteId}/delete`
    return this.httpClient.delete<InviteDto>(url);
  }

  public checkIfExistsInOrganization(orgId: string, userIds: string[]) {
    const url = `${ environment.api_url }/users/org/${ orgId }/invites/exists`
    return this.httpClient.post<PageDto<InviteDto>>(url, userIds);
  }
}
