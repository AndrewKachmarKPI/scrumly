import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MemberDashboardRoutingModule } from './member-dashboard-routing.module';
import { MemberDashboardOutlet } from './layout/member-dashboard-outlet/member-dashboard-outlet.component';
import { MemberDashboardSidebarComponent } from './layout/member-dashboard-sidebar/member-dashboard-sidebar.component';
import { MemberDashboardHeaderComponent } from './layout/member-dashboard-header/member-dashboard-header.component';
import { HomeModule } from './modules/home/home.module';
import { SettingsModule } from './modules/settings/settings.module';
import { PrimeComponentsModule } from '../ui-components/scrumly-components/prime-components.module';
import { PrimeLibComponentsModule } from '../ui-components/prime-lib-components/prime-lib-components.module';
import { OrganizationsModule } from './modules/organizations/organizations.module';
import { InvitesModule } from './modules/invites/invites.module';
import { EventsModule } from './modules/events/events.module';
import { ActivityLibraryModule } from './modules/activity-library/activity-library.module';
import { BacklogModule } from './modules/backlog/backlog.module';
import { ChatsModule } from './modules/chats/chats.module';

@NgModule({
  declarations: [
    MemberDashboardOutlet,
    MemberDashboardSidebarComponent,
    MemberDashboardHeaderComponent,
  ],
  imports: [
    CommonModule,
    MemberDashboardRoutingModule,
    HomeModule,
    SettingsModule,
    PrimeComponentsModule,
    PrimeLibComponentsModule,
    OrganizationsModule,
    InvitesModule,
    EventsModule,
    ActivityLibraryModule,
    BacklogModule,
    ChatsModule
  ]
})
export class MemberDashboardModule {
}
