import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { environment } from "../../../enviroments/enviroment";
import { Observable } from "rxjs";
import { ConferenceConfigDto, ConferenceRoomDto } from '../conference/model/conference.model';
import { UserProfileDto } from "../../auth/auth.model";

@Injectable({
  providedIn: 'root'
})
export class ConferenceService {
  private apiUrl = `${ environment.api_url }/conference/api/conference`;

  constructor(private http: HttpClient) {
  }

  joinConference(conferenceId: string): Observable<ConferenceRoomDto> {
    return this.http.post<ConferenceRoomDto>(this.apiUrl + '/' + conferenceId, {});
  }

  isJoined(conferenceId: string): Observable<boolean> {
    return this.http.get<boolean>(this.apiUrl + '/' + conferenceId + '/isJoined', {});
  }

  exitConference(conferenceId: string) {
    return this.http.post<ConferenceRoomDto>(this.apiUrl + '/' + conferenceId + '/exit', {});
  }

  kickFromConference(conferenceId: string, userId: string) {
    return this.http.post<ConferenceRoomDto>(this.apiUrl + '/' + conferenceId + '/' + userId, {});
  }

  closeConference(conferenceId: string) {
    return this.http.post(this.apiUrl + '/' + conferenceId + '/close', {});
  }

  getActiveSessions(conferenceId: string): Observable<UserProfileDto[]> {
    return this.http.get<UserProfileDto[]>(this.apiUrl + '/' + conferenceId + '/sessions', {});
  }

  changeRemoteConferenceConfig(conferenceId: string, config: ConferenceConfigDto) {
    return this.http.put(this.apiUrl + '/' + conferenceId + '/remote/config', config);
  }
}
