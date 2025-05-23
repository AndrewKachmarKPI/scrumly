import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ScheduledEventsPageComponent } from "./page/scheduled-events/scheduled-events-page.component";

const routes: Routes = [
  {
    path: '',
    redirectTo: ':teamId/meetings',
    pathMatch: 'full'
  },
  {
    path: 'meetings',
    component: ScheduledEventsPageComponent
  },
  {
    path: ':teamId/meetings',
    component: ScheduledEventsPageComponent
  }
];


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EventsRoutingModule {
}
