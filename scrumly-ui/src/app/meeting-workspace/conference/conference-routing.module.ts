import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { JoinConferencePageComponent } from './pages/join-conference-page/join-conference-page.component';
import { ConferenceDisconnectedComponent } from './pages/conference-disconnected/conference-disconnected.component';

const routes: Routes = [
  {
    path: ':conferenceId',
    component: JoinConferencePageComponent
  },
  {
    path: ':conferenceId/disconnected',
    component: ConferenceDisconnectedComponent
  }
];

@NgModule({
  imports: [ RouterModule.forChild(routes) ],
  exports: [ RouterModule ]
})
export class ConferenceRoutingModule {
}
