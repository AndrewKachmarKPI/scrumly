import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { OrgDashboardComponent } from "./org-dashboard/org-dashboard.component";
import { OrgListComponent } from "./org-list/org-list.component";
import { OrgMembersComponent } from "./org-members/org-members.component";
import { OrgSettingsComponent } from "./org-settings/org-settings.component";
import { OrgTeamsComponent } from "./org-teams/org-teams.component";
import { OrgIntegrationsComponent } from "./org-integrations/org-integrations.component";
import { OrgInvitesComponent } from "./org-invites/org-invites.component";

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'list'
  },
  {
    path: 'list',
    component: OrgListComponent
  },
  {
    path: ':orgId/dashboard',
    component: OrgDashboardComponent
  },
  {
    path: ':orgId/teams',
    component: OrgTeamsComponent
  },
  {
    path: ':orgId/members',
    component: OrgMembersComponent
  },
  {
    path: ':orgId/invites',
    component: OrgInvitesComponent
  },
  {
    path: ':orgId/integrations',
    component: OrgIntegrationsComponent
  },
  {
    path: ':orgId/settings',
    component: OrgSettingsComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class OrganizationsRoutingModule {
}
