import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import {
  CreateTeamRQ,
  OrganizationTeamGroupDto,
  TeamDto,
  TeamMemberDto,
  UpdateTeamRQ
} from '../model/organization.model';
import { environment } from '../../../../../enviroments/enviroment';
import { PageDto, SearchQuery } from '../../../../ui-components/models/search-filter.model';

@Injectable({
  providedIn: 'root'
})
export class TeamService {
  constructor(private httpClient: HttpClient) {
  }

  // HTTP METHODS

  public createTeam(createTeamRQ: CreateTeamRQ): Observable<TeamDto> {
    const url = `${ environment.api_url }/users/team`;
    return this.httpClient.post<TeamDto>(url, createTeamRQ);
  }

  public updateTeam(teamId: string, updateTeamRQ: UpdateTeamRQ): Observable<TeamDto> {
    const url = `${ environment.api_url }/users/team/${ teamId }`;
    return this.httpClient.put<TeamDto>(url, updateTeamRQ);
  }

  public findTeams(searchQuery: SearchQuery): Observable<PageDto<TeamDto>> {
    const url = `${ environment.api_url }/users/team/all`;
    return this.httpClient.post<PageDto<TeamDto>>(url, searchQuery);
  }

  public findMyTeams(searchQuery: SearchQuery): Observable<PageDto<TeamDto>> {
    const url = `${ environment.api_url }/users/team/me`;
    return this.httpClient.post<PageDto<TeamDto>>(url, searchQuery);
  }

  public findOrganizationTeamGroup(): Observable<OrganizationTeamGroupDto[]> {
    const url = `${ environment.api_url }/users/team/groups/me`;
    return this.httpClient.get<OrganizationTeamGroupDto[]>(url);
  }

  public findTeamsByOrganizationId(orgId: string, searchQuery: SearchQuery): Observable<PageDto<TeamDto>> {
    const url = `${ environment.api_url }/users/team/organization/${ orgId }`;
    return this.httpClient.post<PageDto<TeamDto>>(url, searchQuery);
  }

  public findTeamById(teamId: string): Observable<TeamDto> {
    const url = `${ environment.api_url }/users/team/${ teamId }`;
    return this.httpClient.get<TeamDto>(url);
  }

  public deleteTeamById(teamId: string): Observable<void> {
    const url = `${ environment.api_url }/users/team/${ teamId }`;
    return this.httpClient.delete<void>(url);
  }

  public inviteTeamMembers(teamId: string, inviteMembers: string[]): Observable<TeamDto> {
    const url = `${ environment.api_url }/users/team/${ teamId }/invite`;
    return this.httpClient.post<TeamDto>(url, inviteMembers);
  }

  public removeTeamMembers(teamId: string, members: string[]): Observable<TeamDto> {
    const url = `${ environment.api_url }/users/team/${ teamId }/remove`;
    return this.httpClient.post<TeamDto>(url, members);
  }

  public checkIfExistsInTeam(teamId: string, userIds: string[]): Observable<boolean> {
    const url = `${ environment.api_url }/users/team/${ teamId }/check`;
    return this.httpClient.post<boolean>(url, userIds);
  }

}
