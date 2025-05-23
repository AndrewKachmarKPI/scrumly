import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { KeycloakGuard } from "./auth/keycloak/keycloak.guard";

const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./public/public-routing.module').then(m => m.PublicRoutingModule)
  },
  {
    path: 'app',
    canActivate: [KeycloakGuard],
    loadChildren: () => import('./member-dashboard/member-dashboard-routing.module').then(m => m.MemberDashboardRoutingModule)
  },
  {
    path: 'app/workspace',
    canActivate: [KeycloakGuard],
    loadChildren: () => import('./meeting-workspace/meeting-workspace-routing.module').then(m => m.MeetingWorkspaceRoutingModule)
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
