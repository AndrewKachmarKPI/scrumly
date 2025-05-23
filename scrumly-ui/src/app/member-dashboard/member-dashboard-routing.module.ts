import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MemberDashboardOutlet } from './layout/member-dashboard-outlet/member-dashboard-outlet.component';

const routes: Routes = [
  {
    path: '',
    component: MemberDashboardOutlet,
    children: [
      {
        path: 'home',
        loadChildren: () => import('./modules/home/home-routing.module').then(m => m.HomeRoutingModule)
      },
      {
        path: 'org',
        loadChildren: () => import('./modules/organizations/organizations-routing.module').then(m => m.OrganizationsRoutingModule)
      },
      {
        path: 'teams',
        loadChildren: () => import('./modules/teams/teams-routing.module').then(m => m.TeamsRoutingModule)
      },
      {
        path: 'chats',
        loadChildren: () => import('./modules/chats/chats.module').then(m => m.ChatsModule)
      },
      {
        path: 'activity',
        loadChildren: () => import('./modules/activity-library/activity-library-routing.module').then(m => m.ActivityLibraryRoutingModule)
      },
      {
        path: 'backlog',
        loadChildren: () => import('./modules/backlog/backlog.module').then(m => m.BacklogModule)
      },
      {
        path: 'calendar',
        loadChildren: () => import('./modules/callendar/calendar-routing.module').then(m => m.CalendarRoutingModule)
      },
      {
        path: 'invites',
        loadChildren: () => import('./modules/invites/invites-routing.module').then(m => m.InvitesRoutingModule)
      },
      {
        path: 'events',
        loadChildren: () => import('./modules/events/events-routing.module').then(m => m.EventsRoutingModule)
      },
      {
        path: 'settings',
        loadChildren: () => import('./modules/settings/settings-routing.module').then(m => m.SettingsRoutingModule)
      }
    ]
  },
  {
    path: 'invites/:inviteId',
    loadChildren: () => import('./modules/invites/invites-actions/invites-actions-routing.module').then(m => m.InvitesActionsRoutingModule)
  },
  {
    path: 'integration',
    loadChildren: () => import('./modules/integration/integration-routing.module').then(m => m.IntegrationRoutingModule)
  }
];

@NgModule({
  imports: [ RouterModule.forChild(routes) ],
  exports: [ RouterModule ]
})
export class MemberDashboardRoutingModule {
}
