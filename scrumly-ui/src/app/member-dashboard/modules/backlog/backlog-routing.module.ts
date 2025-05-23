import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TeamBacklogsComponent } from './pages/team-backlog/team-backlogs.component';
import { BacklogIssuesComponent } from './pages/backlog-issues/backlog-issues.component';
import { BacklogIssueTypesComponent } from './pages/backlog-issue-types/backlog-issue-types.component';
import { BacklogIssueStatusesComponent } from './pages/backlog-issue-statuses/backlog-issue-statuses.component';
import { BacklogIssueViewComponent } from './pages/backlog-issue-view/backlog-issue-view.component';

const routes: Routes = [
  {
    path: ':teamId/list',
    component: TeamBacklogsComponent
  },
  {
    path: 'issues',
    component: BacklogIssuesComponent
  },
  {
    path: 'issue-types',
    component: BacklogIssueTypesComponent
  },
  {
    path: 'issue-statuses',
    component: BacklogIssueStatusesComponent
  },
  {
    path: ':issueKey/issue',
    component: BacklogIssueViewComponent
  },
];

@NgModule({
  imports: [ RouterModule.forChild(routes) ],
  exports: [ RouterModule ]
})
export class BacklogRoutingModule {
}
