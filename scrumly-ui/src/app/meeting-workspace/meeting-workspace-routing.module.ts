import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { KeycloakGuard } from "../auth/keycloak/keycloak.guard";

const routes: Routes = [
  {
    path: 'dashboard',
    canActivate: [KeycloakGuard],
    loadChildren: () => import('./workspace/workspace-routing.module').then(m => m.WorkspaceRoutingModule)
  },
  {
    path: 'conference',
    canActivate: [KeycloakGuard],
    loadChildren: () => import('./conference/conference-routing.module').then(m => m.ConferenceRoutingModule)
  },
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MeetingWorkspaceRoutingModule {
}
