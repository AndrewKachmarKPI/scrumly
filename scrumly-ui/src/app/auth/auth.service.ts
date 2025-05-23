import { Injectable } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { environment } from '../../enviroments/enviroment';
import { KeycloakProfile, KeycloakTokenParsed } from 'keycloak-js';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, from, lastValueFrom, Observable, of } from 'rxjs';
import { RegisterUserRQ, UserInfoDto, UserProfileDto, UserProfileRQ } from './auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private profileUpdateSubject = new BehaviorSubject<UserProfileDto | null>(null);
  public onProfileUpdate = this.profileUpdateSubject.asObservable();

  constructor(private readonly keycloakService: KeycloakService,
              private readonly httpClient: HttpClient) {
    if (this.isLoggedIn()) {
      this.updateCurrentUser();
    }
  }

  getAccessToken(): Observable<string> {
    return from(this.keycloakService.getToken());
  }


  public updateCurrentUser() {
    this.loadUserProfile().subscribe(profile => {
      this.updateProfileData(profile);
    });
  }

  public updateToken(minTime: number): Promise<boolean> {
    return this.keycloakService.updateToken(minTime);
  }

  public isTokenExpired(minValidity: number) {
    return this.keycloakService.isTokenExpired(minValidity);
  }

  public register(data: RegisterUserRQ) {

    const url = `${ environment.api_url }/users/register`
    return this.httpClient.post(url, data);
  }

  public loadUserProfile(): Observable<UserProfileDto> {
    const url = `${ environment.api_url }/users/me`
    return this.httpClient.get<UserProfileDto>(url);
  }

  public findUserInfoProfile(username: string): Observable<UserInfoDto> {
    const url = `${ environment.api_url }/users/${ username }/info`
    return this.httpClient.get<UserInfoDto>(url);
  }

  public findUserInfoProfileAutocomplete(query: string): Observable<UserInfoDto[]> {
    const url = `${ environment.api_url }/users/${ query }/autocomplete`
    return this.httpClient.get<UserInfoDto[]>(url);
  }

  public findUserInfoProfileAutocompleteOrg(query: string, orgId: string): Observable<UserInfoDto[]> {
    const url = `${ environment.api_url }/users/${ query }/autocomplete/${ orgId }/org`
    return this.httpClient.get<UserInfoDto[]>(url);
  }

  public updateProfile(profile: UserProfileRQ, file: File): Observable<UserProfileDto> {
    const url = `${ environment.api_url }/users/me`
    const form: FormData = new FormData();
    form.append('userProfileRQ', new Blob([ JSON.stringify(profile) ], { type: 'application/json' }));
    form.append('profileImage', file);
    return this.httpClient.put<UserProfileDto>(url, form);
  }

  public updateProfileData(userProfile: UserProfileDto) {
    this.profileUpdateSubject.next(userProfile);
  }

  public isCurrentUser(username: string) {
    return this.profileUpdateSubject.getValue()?.userId === username;
  }

  public getCurrentUser() {
    return this.profileUpdateSubject.getValue();
  }

  public getUserId() {
    return this.profileUpdateSubject.getValue()?.userId;
  }


  public getLoggedUser(): KeycloakTokenParsed | undefined {
    try {
      return this.keycloakService.getKeycloakInstance().idTokenParsed;
    } catch (e) {
      console.error('Exception', e);
      return undefined;
    }
  }

  public getToken(): Promise<string> {
    return this.keycloakService.getToken();
  }

  public getUsername(): string {
    return this.keycloakService.getUsername();
  }


  public isLoggedIn(): boolean {
    return this.keycloakService.isLoggedIn();
  }

  public loadUserProfileKeycloak(): Observable<KeycloakProfile> {
    return from(this.keycloakService.loadUserProfile());
  }

  public login(options?: Keycloak.KeycloakLoginOptions): void {
    this.keycloakService.login(options);
  }

  public logout(): void {
    this.keycloakService.logout(environment.keycloak.postLogoutRedirectUri);
  }

  public redirectToProfile(): void {
    this.keycloakService.getKeycloakInstance().accountManagement();
  }

  public getRoles(): string[] {
    return this.keycloakService.getUserRoles();
  }

  public hasRole(role: string): boolean {
    return this.getRoles().some(userRole => userRole.includes(role));
  }
}
