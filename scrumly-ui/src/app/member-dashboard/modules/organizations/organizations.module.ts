import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { OrganizationsRoutingModule } from './organizations-routing.module';
import { OrgMembersComponent } from './org-members/org-members.component';
import { OrgDashboardComponent } from './org-dashboard/org-dashboard.component';
import { OrgListComponent } from './org-list/org-list.component';
import { OrgTeamsComponent } from './org-teams/org-teams.component';
import { OrgSettingsComponent } from './org-settings/org-settings.component';
import { OrganizationService } from "./services/organization.service";
import { TranslatePipe } from "@ngx-translate/core";
import { UiComponentsModule } from "../../../ui-components/ui-components.module";
import {
  OrgCreateDialogComponent
} from './dialogs/org-create-dialog/org-create-dialog.component';
import { DialogService } from "primeng/dynamicdialog";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { OrgShortInfoComponent } from './org-list/org-header/org-short-info.component';
import { OrgMemberHistoryDialogComponent } from './dialogs/org-member-history-dialog/org-member-history-dialog.component';
import { OrgListPickerComponent } from './org-list/org-list-picker/org-list-picker.component';
import { TeamCreateDialogComponent } from './dialogs/team-create-dialog/team-create-dialog.component';
import { OrgTeamMembersTableComponent } from './org-teams/org-team-members-table/org-team-members-table.component';
import { TeamInviteMembersDialogComponent } from './dialogs/team-invite-members-dialog/team-invite-members-dialog.component';
import { OrgIntegrationsComponent } from "./org-integrations/org-integrations.component";
import { IntegrationModule } from "../integration/integration.module";
import { OrgTeamDropdownComponent } from './components/org-team-dropdown/org-team-dropdown.component';
import { OrgTeamMembersListComponent } from './components/org-team-members-list/org-team-members-list.component';
import { OrgTeamViewComponent } from './components/org-team-view/org-team-view.component';


@NgModule({
  declarations: [
    OrgMembersComponent,
    OrgDashboardComponent,
    OrgListComponent,
    OrgTeamsComponent,
    OrgSettingsComponent,
    OrgIntegrationsComponent,
    OrgCreateDialogComponent,
    OrgShortInfoComponent,
    OrgMemberHistoryDialogComponent,
    OrgListPickerComponent,
    TeamCreateDialogComponent,
    OrgTeamMembersTableComponent,
    TeamInviteMembersDialogComponent,
    OrgTeamDropdownComponent,
    OrgTeamMembersListComponent,
    OrgTeamViewComponent
  ],
  imports: [
    CommonModule,
    OrganizationsRoutingModule,
    TranslatePipe,
    UiComponentsModule,
    ReactiveFormsModule,
    FormsModule,
    IntegrationModule
  ],
  exports: [
    OrgShortInfoComponent,
    OrgListPickerComponent,
    OrgTeamDropdownComponent,
    OrgTeamDropdownComponent,
    OrgTeamMembersListComponent,
    OrgTeamViewComponent
  ],
  providers: [
    OrganizationService,
    DialogService
  ]
})
export class OrganizationsModule {
}
