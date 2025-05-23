import { Injectable } from '@angular/core';
import { environment } from "../../../../../enviroments/enviroment";
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, Observable } from "rxjs";
import { PageDto, SearchQuery } from "../../../../ui-components/models/search-filter.model";
import { CreateActivityRQ, EventDto } from "../model/event.model";
import { ActivityDto } from "../model/activity.model";
import { EventPreSelection } from "../components/schedule-event-sidebar/schedule-event-sidebar.component";

@Injectable({
  providedIn: 'root'
})
export class EventService {
  scheduleEventSidebarState: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  onScheduleEventSidebarState = this.scheduleEventSidebarState.asObservable();

  templateSelectionState: BehaviorSubject<EventPreSelection| undefined> = new BehaviorSubject<EventPreSelection | undefined>(undefined);
  onTemplateSelectionState = this.templateSelectionState.asObservable();

  editEventSidebarState: BehaviorSubject<ActivityDto | undefined> = new BehaviorSubject<ActivityDto | undefined>(undefined);
  onEditEventSidebarState = this.editEventSidebarState.asObservable();

  private apiUrl = `${ environment.api_url }/events/event`;

  constructor(private http: HttpClient) {
  }

  getAllEvents(): Observable<EventDto[]> {
    return this.http.get<EventDto[]>(this.apiUrl);
  }

  getAllEventsByCreatedFor(createdFor: string): Observable<EventDto[]> {
    return this.http.get<EventDto[]>(`${ this.apiUrl }/createdFor/${ createdFor }`);
  }

  findEvents(searchQuery: SearchQuery): Observable<PageDto<EventDto>> {
    return this.http.post<PageDto<EventDto>>(`${ this.apiUrl }/search`, searchQuery);
  }

  getEventById(eventId: string): Observable<EventDto> {
    return this.http.get<EventDto>(`${ this.apiUrl }/${ eventId }`);
  }

  createEvent(createEventRQ: CreateActivityRQ): Observable<EventDto> {
    return this.http.post<EventDto>(this.apiUrl, createEventRQ);
  }

  updateEvent(eventId: string, eventDTO: EventDto): Observable<EventDto> {
    return this.http.put<EventDto>(`${ this.apiUrl }/${ eventId }`, eventDTO);
  }

  deleteEvent(eventId: string): Observable<void> {
    return this.http.delete<void>(`${ this.apiUrl }/${ eventId }`);
  }
}
