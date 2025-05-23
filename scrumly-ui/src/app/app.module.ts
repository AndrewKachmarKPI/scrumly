import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthModule } from './auth/auth.module';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { PublicModule } from './public/public.module';
import { UiComponentsModule } from './ui-components/ui-components.module';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MemberDashboardModule } from './member-dashboard/member-dashboard.module';
import { OrganizationsModule } from './member-dashboard/modules/organizations/organizations.module';
import { ClipboardModule } from '@angular/cdk/clipboard'
import { CalendarModule } from './member-dashboard/modules/callendar/calendar.module';
import { EventsModule } from './member-dashboard/modules/events/events.module';
import { MeetingWorkspaceModule } from './meeting-workspace/meeting-workspace.module';
import { TeamsModule } from './member-dashboard/modules/teams/teams.module';

export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http);
}

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    UiComponentsModule,
    HttpClientModule,
    ClipboardModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [ HttpClient ]
      }
    }),
    AuthModule,
    PublicModule,
    MemberDashboardModule,
    OrganizationsModule,
    CalendarModule,
    EventsModule,
    TeamsModule,
    MeetingWorkspaceModule
  ],
  bootstrap: [ AppComponent ],
  providers: [
    // {
    //   provide: HTTP_INTERCEPTORS,
    //   useClass: AuthInterceptor,
    //   multi: true
    // }
  ]
})
export class AppModule {
}
