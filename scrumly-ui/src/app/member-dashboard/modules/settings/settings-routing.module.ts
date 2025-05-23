import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MemberProfileComponent } from "./pages/member-profile/member-profile.component";
import { MemberIntegrationsComponent } from "./pages/member-integrations/member-integrations.component";
import { MemberInvitesComponent } from "./pages/member-invites/member-invites.component";

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'profile'
  },
  {
    path: 'profile',
    component: MemberProfileComponent
  },
  {
    path: 'invites',
    component: MemberInvitesComponent
  },
  {
    path: 'integrations',
    component: MemberIntegrationsComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SettingsRoutingModule {
}
