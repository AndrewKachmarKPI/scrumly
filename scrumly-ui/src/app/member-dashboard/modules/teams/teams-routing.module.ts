import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MyTeamsComponent } from './pages/my-teams/my-teams.component';
import { MyTeamComponent } from './pages/my-team/my-team.component';

const routes: Routes = [
  {
    path: ':teamId/dashboard',
    component: MyTeamComponent
  },
  {
    path: 'my',
    component: MyTeamsComponent
  }
];

@NgModule({
  imports: [ RouterModule.forChild(routes) ],
  exports: [ RouterModule ]
})
export class TeamsRoutingModule {
}
