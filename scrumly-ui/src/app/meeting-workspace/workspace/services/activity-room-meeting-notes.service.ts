import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ActivityRoomMeetingNotesService {


  constructor(private http: HttpClient) {
  }


}
