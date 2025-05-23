import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject, takeUntil } from 'rxjs';
import { environment } from '../../../../../enviroments/enviroment';
import {
  CreateOrganizationRQ, OrganizationDto,
  OrganizationInfoDto, OrganizationTeamGroupDto, TeamDto,
} from '../model/organization.model';
import { PageDto, SearchQuery } from '../../../../ui-components/models/search-filter.model';
import { ActivatedRoute, Router } from '@angular/router';
import { TeamService } from './team.service';
import { AuthService } from '../../../../auth/auth.service';

export interface OrganizationGroupSelection {
  org?: OrganizationInfoDto | OrganizationDto,
  team?: TeamDto
}

@Injectable({
  providedIn: 'root'
})
export class OrganizationService implements OnDestroy {
  organizationTeamGroup: BehaviorSubject<OrganizationTeamGroupDto[]> = new BehaviorSubject<OrganizationTeamGroupDto[]>([]);
  organizationTeamGroupSub = this.organizationTeamGroup.asObservable();

  selectedOrganizationGroup: BehaviorSubject<OrganizationGroupSelection | undefined> = new BehaviorSubject<OrganizationGroupSelection | undefined>(undefined);
  onOrganizationSelectionChange: Observable<OrganizationGroupSelection | undefined> = this.selectedOrganizationGroup.asObservable();

  onOrganizationEventSubject: BehaviorSubject<boolean> = new BehaviorSubject(false);
  onOrganizationEvent = this.onOrganizationEventSubject.asObservable();

  private destroy$ = new Subject<void>();

  constructor(private httpClient: HttpClient,
              private teamService: TeamService,
              private authService: AuthService,
              private router: Router,
              private route: ActivatedRoute) {
    this.onOrganizationEvent
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.loadOrganizationTeamGroup();
      })
    this.loadOrganizationTeamGroup();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }


  loadOrganizationTeamGroup() {
    this.teamService.findOrganizationTeamGroup()
      .pipe(takeUntil(this.destroy$))
      .subscribe((groups) => {
        this.organizationTeamGroup.next(groups);
        const selection = this.getOrganizationGroupSelection(groups);
        this.updateOrganizationSelection(selection);
      });
  }

  getOrganizationGroupSelection(groups: OrganizationTeamGroupDto[]) {
    const organizationGroupSelection = localStorage.getItem('organizationGroupSelection');
    const groupSelection: OrganizationGroupSelection | undefined = organizationGroupSelection
      ? JSON.parse(organizationGroupSelection)
      : undefined;
    const selection: OrganizationGroupSelection = {};
    if (groupSelection?.org) {
      const selectedGroup = groups.find(group => group.organization.organizationId === groupSelection.org?.organizationId);
      const selectedOrg = selectedGroup?.organization;
      if (selectedOrg) {
        selection.org = selectedOrg;
        if (groupSelection.team) {
          selection.team = selectedGroup?.teams?.find(team => team.teamId === groupSelection?.team?.teamId)
        }
      }
    } else if (groups.length > 0) {
      selection.org = groups[0].organization;
      selection.team = groups[0].teams?.[0];
      localStorage.setItem('organizationGroupSelection', JSON.stringify(selection));
    }
    return selection;
  }


  getCurrentOrganizationId(): string {
    return this.selectedOrganizationGroup.getValue()?.org?.organizationId!;
  }

  getCurrentTeamId(): string {
    return this.selectedOrganizationGroup.getValue()?.team?.teamId!;
  }

  updateOrganizationSelection(selection: OrganizationGroupSelection) {
    this.selectedOrganizationGroup.next(selection);

    const urlParts = this.router.url.split('/').filter(Boolean);
    if (this.router.url.includes('org')) {
      if (urlParts.length >= 4) {
        urlParts[2] = selection.org?.organizationId!;
      }
    } else if (this.router.url.includes('events')) {
      if (urlParts.length >= 4) {
        urlParts[2] = selection.team?.teamId!;
      }
    } else if (this.router.url.includes('team')) {
      if (urlParts.length >= 4) {
        urlParts[2] = selection.team?.teamId!;
      }
    } else if (this.router.url.includes('teams')) {
      if (urlParts.length >= 4) {
        urlParts[2] = selection.team?.teamId!;
      }
    } else if (this.router.url.includes('backlog') && this.router.url.includes('list')) {
      if (urlParts.length >= 4) {
        urlParts[2] = selection.team?.teamId!;
      }
    }

    const newUrl = urlParts.join('/');
    this.router.navigate([ newUrl ], {
      onSameUrlNavigation: 'reload'
    });
  }

  // HTTP METHODS

  public createOrganization(rq: CreateOrganizationRQ, file: File): Observable<OrganizationInfoDto> {
    const url = `${ environment.api_url }/users/org`
    const form: FormData = new FormData();
    form.append('rq', new Blob([ JSON.stringify(rq) ], { type: 'application/json' }));
    form.append('logo', file);
    return this.httpClient.post<OrganizationInfoDto>(url, form);
  }

  public updateOrganization(orgId: string, profile: OrganizationInfoDto, file: File): Observable<OrganizationInfoDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }`
    const form: FormData = new FormData();
    form.append('rq', new Blob([ JSON.stringify(profile) ], { type: 'application/json' }));
    form.append('logo', file);
    return this.httpClient.put<OrganizationInfoDto>(url, form);
  }

  public findMyOrganizations(searchQuery: SearchQuery): Observable<PageDto<OrganizationInfoDto>> {
    const url = `${ environment.api_url }/users/org/me`
    return this.httpClient.post<PageDto<OrganizationInfoDto>>(url, searchQuery);
  }

  public findOrganizationInfoList(orgIds: string[]): Observable<OrganizationInfoDto[]> {
    const url = `${ environment.api_url }/users/org/info`
    return this.httpClient.post<OrganizationInfoDto[]>(url, orgIds);
  }

  public findOrganizationById(orgId: string): Observable<OrganizationInfoDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }/info`
    return this.httpClient.get<OrganizationInfoDto>(url);
  }

  public archiveOrganization(orgId: string): Observable<OrganizationInfoDto> {
    const url = `${ environment.api_url }/users/org/${ orgId }/archive`
    return this.httpClient.delete<OrganizationInfoDto>(url);
  }

  public deleteOrganization(orgId: string): Observable<void> {
    const url = `${ environment.api_url }/users/org/${ orgId }/delete`
    return this.httpClient.delete<void>(url);
  }
}
