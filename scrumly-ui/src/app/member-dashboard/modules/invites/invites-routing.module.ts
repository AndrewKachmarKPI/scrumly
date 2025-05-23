import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { OrgInvitesComponent } from "../organizations/org-invites/org-invites.component";

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'me'
  },
  {
    path: 'org/:orgId',
    component: OrgInvitesComponent
  },
  {
    path: 'team/:orgId',
    component: OrgInvitesComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InvitesRoutingModule {
}
