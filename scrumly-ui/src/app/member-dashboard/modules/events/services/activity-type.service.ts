import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from "../../../../../enviroments/enviroment";
import { ActivityTypeDto } from "../model/activity.model";

@Injectable({
  providedIn: 'root'
})
export class ActivityTypeService {
  private apiUrl = `${ environment.api_url }/events/activity-types`;

  constructor(private http: HttpClient) {
  }

  createActivityType(dto: ActivityTypeDto): Observable<ActivityTypeDto> {
    return this.http.post<ActivityTypeDto>(this.apiUrl, dto);
  }

  updateActivityType(id: number, dto: ActivityTypeDto): Observable<ActivityTypeDto> {
    return this.http.put<ActivityTypeDto>(`${ this.apiUrl }/${ id }`, dto);
  }

  getActivityTypeById(id: number): Observable<ActivityTypeDto> {
    return this.http.get<ActivityTypeDto>(`${ this.apiUrl }/${ id }`);
  }

  deleteActivityTypeById(id: number): Observable<void> {
    return this.http.delete<void>(`${ this.apiUrl }/${ id }`);
  }

  getAllActivityTypes(): Observable<ActivityTypeDto[]> {
    return this.http.get<ActivityTypeDto[]>(this.apiUrl);
  }
}
