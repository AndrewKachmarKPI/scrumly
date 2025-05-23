import { Injectable } from '@angular/core';
import { PageDto, SearchQuery } from '../../../../ui-components/models/search-filter.model';
import { BehaviorSubject, filter, Observable, Subject, takeUntil } from 'rxjs';
import { OrganizationMemberDto, OrganizationMemberRole } from '../model/organization.model';
import { environment } from '../../../../../enviroments/enviroment';
import { HttpClient } from '@angular/common/http';
import { OrganizationService } from './organization.service';
import { TranslateService } from '@ngx-translate/core';
import { UserProfileDto } from '../../../../auth/auth.model';

@Injectable({
  providedIn: 'root'
})
export class OrganizationMemberService {
  private orgMemberUpdateSubject = new BehaviorSubject<OrganizationMemberDto | null>(null);
  public onOrgMemberUpdate = this.orgMemberUpdateSubject.asObservable();

  private destroy$: Subject<void> = new Subject<void>();

  constructor(private httpClient: HttpClient,
              private organizationService: OrganizationService,
              private translateService: TranslateService) {
    this.organizationService.onOrganizationSelectionChange
      .pipe(filter(Boolean), takeUntil(this.destroy$))
      .subscribe({
        next: (org) => {
          if (org.org) {
            this.loadOrganizationMember(org.org?.organizationId!);
          }
        }
      });
  }

  private loadOrganizationMember(organizationId: string) {
    this.findMyOrganizationMemberAccount(organizationId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (profile) => {
          this.orgMemberUpdateSubject.next(profile);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getCurrentOrganizationMember() {
    return this.orgMemberUpdateSubject.getValue();
  }


  public findOrganizationMembers(orgId: string, searchQuery: SearchQuery): Observable<PageDto<OrganizationMemberDto>> {
    const url = `${ environment.api_url }/users/org/${ orgId }/members`
    return this.httpClient.post<PageDto<OrganizationMemberDto>>(url, searchQuery);
  }

  public findMyOrganizationMemberAccount(orgId: string): Observable<OrganizationMemberDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }/members/me`
    return this.httpClient.get<OrganizationMemberDto>(url);
  }

  public changeMemberRole(orgId: string, username: string, memberRole: OrganizationMemberRole): Observable<OrganizationMemberDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }/members/${ username }/role`;
    return this.httpClient.put<OrganizationMemberDto>(url, {}, {
      params: {
        role: memberRole
      }
    });
  }

  public blockMember(orgId: string, username: string): Observable<OrganizationMemberDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }/members/${ username }/block`;
    return this.httpClient.put<OrganizationMemberDto>(url, null);
  }

  public activateMember(orgId: string, username: string): Observable<OrganizationMemberDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }/members/${ username }/activate`;
    return this.httpClient.put<OrganizationMemberDto>(url, null);
  }

  public removeMember(orgId: string, username: string): Observable<void> {
    const url = `${ environment.api_url }/users/org/${ orgId }/members/${ username }`;
    return this.httpClient.delete<void>(url);
  }

  public getMemberRoleOptions() {
    const values: string[] = Object.values(OrganizationMemberRole);
    return values.map(value => {
      return {
        value: value,
        name: this.translateService.instant('organizations.members.MEMBER_ROLE.' + value)
      }
    })
  }
}
