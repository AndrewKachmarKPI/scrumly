import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AcceptInviteFormComponent } from "./pages/accept-invite-form/accept-invite-form.component";
import { InvitesActionsOutletComponent } from "./layout/invites-actions-outlet/invites-actions-outlet.component";

const routes: Routes = [
  {
    path: '',
    component: InvitesActionsOutletComponent,
    children: [
      {
        path: '',
        component: AcceptInviteFormComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InvitesActionsRoutingModule {
}
