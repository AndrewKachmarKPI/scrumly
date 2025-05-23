import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GoogleCalendarCallbackComponent } from "./pages/google-calendar-callback/google-calendar-callback.component";
import { JiraCloudCallbackComponent } from "./pages/jira-cloud-callback/jira-cloud-callback.component";

const routes: Routes = [
  {
    path: 'google',
    children: [
      {
        path: 'calendar',
        component: GoogleCalendarCallbackComponent
      }
    ]
  },
  {
    path: 'jira',
    children: [
      {
        path: 'cloud',
        component: JiraCloudCallbackComponent
      }
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class IntegrationRoutingModule {
}
