import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { BacklogRoutingModule } from './backlog-routing.module';
import { UiComponentsModule } from '../../../ui-components/ui-components.module';
import { TeamBacklogsComponent } from './pages/team-backlog/team-backlogs.component';
import { MyBacklogComponent } from './pages/my-backlog/my-backlog.component';
import { BacklogIssueTypesComponent } from './pages/backlog-issue-types/backlog-issue-types.component';
import { BacklogIssueStatusesComponent } from './pages/backlog-issue-statuses/backlog-issue-statuses.component';
import { BacklogIssuesComponent } from './pages/backlog-issues/backlog-issues.component';
import { TranslatePipe } from '@ngx-translate/core';
import { CreateBacklogDialogComponent } from './dialogs/create-backlog-dialog/create-backlog-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { IssueTypeRowComponent } from './components/issue-type-row/issue-type-row.component';
import { BacklogIssueDialogComponent } from './dialogs/backlog-issue-dialog/backlog-issue-dialog.component';
import { IssueStatusDropdownComponent } from './components/issue-status-dropdown/issue-status-dropdown.component';
import { IssueTypeDropdownComponent } from './components/issue-type-dropdown/issue-type-dropdown.component';
import { IssueAssigneeDropdownComponent } from './components/issue-assignee-dropdown/issue-assignee-dropdown.component';
import { BacklogIssueViewComponent } from './pages/backlog-issue-view/backlog-issue-view.component';
import { ExportIssueDialogComponent } from './dialogs/export-issue-dialog/export-issue-dialog.component';
import { OrganizationsModule } from '../organizations/organizations.module';
import { WorkspaceModule } from '../../../meeting-workspace/workspace/workspace.module';


@NgModule({
    declarations: [
        TeamBacklogsComponent,
        MyBacklogComponent,
        BacklogIssueTypesComponent,
        BacklogIssueStatusesComponent,
        BacklogIssuesComponent,
        CreateBacklogDialogComponent,
        IssueTypeRowComponent,
        BacklogIssueDialogComponent,
        IssueStatusDropdownComponent,
        IssueTypeDropdownComponent,
        IssueAssigneeDropdownComponent,
        BacklogIssueViewComponent,
        ExportIssueDialogComponent
    ],
    exports: [
        IssueAssigneeDropdownComponent
    ],
    imports: [
        CommonModule,
        BacklogRoutingModule,
        UiComponentsModule,
        TranslatePipe,
        ReactiveFormsModule,
        OrganizationsModule,
        WorkspaceModule,
    ]
})
export class BacklogModule { }
