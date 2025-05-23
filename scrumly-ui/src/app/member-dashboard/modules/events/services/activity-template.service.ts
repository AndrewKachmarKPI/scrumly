import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from "../../../../../enviroments/enviroment";
import { ActivityTemplateDto, ActivityTemplateGroupDto, CreateActivityTemplateRQ } from "../model/activity.model";
import { PageDto, SearchQuery } from "../../../../ui-components/models/search-filter.model";

@Injectable({
  providedIn: 'root'
})
export class ActivityTemplateService {
  private apiUrl = `${ environment.api_url }/events/activity-templates`;

  constructor(private http: HttpClient) {
  }

  createActivityTemplate(createRQ: CreateActivityTemplateRQ, previewImg?: File): Observable<ActivityTemplateDto> {
    const formData = new FormData();
    formData.append('createRQ', new Blob([JSON.stringify(createRQ)], { type: 'application/json' }));

    if (previewImg) {
      formData.append('previewImg', previewImg, previewImg.name);
    }

    return this.http.post<ActivityTemplateDto>(this.apiUrl, formData);
  }

  updateActivityTemplate(templateId: string, templateRQ: ActivityTemplateDto, previewImg?: File): Observable<ActivityTemplateDto> {
    const formData = new FormData();
    formData.append('templateRQ', new Blob([JSON.stringify(templateRQ)], { type: 'application/json' }));

    if (previewImg && previewImg instanceof File) {
      formData.append('previewImg', previewImg, previewImg.name);
    }

    return this.http.put<ActivityTemplateDto>(`${ this.apiUrl }/${ templateId }`, formData);
  }

  copyActivityTemplate(templateId: string, ownerId: string): Observable<ActivityTemplateDto> {
    const params = new HttpParams().set('ownerId', ownerId);
    return this.http.post<ActivityTemplateDto>(`${ this.apiUrl }/${ templateId }/copy`, null, { params });
  }

  findActivityTemplates(searchQuery: SearchQuery): Observable<PageDto<ActivityTemplateDto>> {
    return this.http.post<PageDto<ActivityTemplateDto>>(`${ this.apiUrl }/search`, searchQuery);
  }

  findMyActivityTemplates(searchQuery: SearchQuery): Observable<PageDto<ActivityTemplateDto>> {
    return this.http.post<PageDto<ActivityTemplateDto>>(`${ this.apiUrl }/search/my`, searchQuery);
  }

  findMyActivityTemplatesGroup(ownerId?: string): Observable<ActivityTemplateGroupDto[]> {
    return this.http.get<ActivityTemplateGroupDto[]>(`${ this.apiUrl }/my/group`, {
      params: {
        ownerId: ownerId || ""
      }
    });
  }

  findActivityTemplatesInfo(searchQuery: SearchQuery): Observable<PageDto<ActivityTemplateDto>> {
    return this.http.post<PageDto<ActivityTemplateDto>>(`${ this.apiUrl }/search/info`, searchQuery);
  }

  findActivityTemplateById(templateId: string): Observable<ActivityTemplateDto> {
    return this.http.get<ActivityTemplateDto>(`${ this.apiUrl }/${ templateId }`);
  }

  findActivityTemplateByIdAndOwner(templateId: string, ownerId: string): Observable<ActivityTemplateDto> {
    return this.http.get<ActivityTemplateDto>(`${ this.apiUrl }/${ templateId }/owner/${ ownerId }`);
  }

  deleteActivityTemplate(templateId: string, ownerId: string): Observable<void> {
    const params = new HttpParams().set('ownerId', ownerId);
    return this.http.delete<void>(`${ this.apiUrl }/${ templateId }`, { params });
  }
}
