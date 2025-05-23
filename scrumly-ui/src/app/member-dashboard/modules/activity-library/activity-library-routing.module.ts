import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ActivityLibrarySearchComponent } from "./pages/activity-library-search/activity-library-search.component";
import { CreateActivityTemplateComponent } from "./pages/create-activity-template/create-activity-template.component";
import {
  ActivityTemplateDetailsComponent
} from "./pages/activity-template-details/activity-template-details.component";

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'search'
  },
  {
    path: 'search',
    component: ActivityLibrarySearchComponent
  },
  {
    path: 'my-templates',
    component: ActivityLibrarySearchComponent,
    data: {
      isUserTemplates: true
    }
  },
  {
    path: ':teamId/team',
    component: ActivityLibrarySearchComponent,
    data: {
      isTeamTemplates: true
    }
  },
  {
    path: 'create',
    component: CreateActivityTemplateComponent
  },
  {
    path: ':activityId/edit',
    component: CreateActivityTemplateComponent
  },
  {
    path: ':activityId/view',
    component: ActivityTemplateDetailsComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ActivityLibraryRoutingModule {
}
