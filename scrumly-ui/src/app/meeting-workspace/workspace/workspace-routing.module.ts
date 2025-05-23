import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { WorkspacePageComponent } from './pages/workspace-page/workspace-page.component';
import { ConferenceKickedComponent } from './pages/conference-kicked/conference-kicked.component';

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: WorkspacePageComponent
  },
  {
    path: 'kicked',
    component: ConferenceKickedComponent
  }
];

@NgModule({
  imports: [ RouterModule.forChild(routes) ],
  exports: [ RouterModule ]
})
export class WorkspaceRoutingModule {
}
