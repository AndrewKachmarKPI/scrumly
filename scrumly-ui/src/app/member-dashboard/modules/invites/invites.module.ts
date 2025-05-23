import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { InvitesRoutingModule } from './invites-routing.module';
import { OrgInvitesComponent } from "../organizations/org-invites/org-invites.component";
import {
  OrgInviteHistoryDialogComponent
} from "../organizations/dialogs/org-invite-history-dialog/org-invite-history-dialog.component";
import {
  OrgInviteMembersDialogComponent
} from "../organizations/dialogs/org-invite-members-dialog/org-invite-members-dialog.component";
import { ReactiveFormsModule } from "@angular/forms";
import { UiComponentsModule } from "../../../ui-components/ui-components.module";
import { TranslateModule } from "@ngx-translate/core";
import { InvitesActionsModule } from "./invites-actions/invites-actions.module";


@NgModule({
  declarations: [
    OrgInvitesComponent,
    OrgInviteHistoryDialogComponent,
    OrgInviteMembersDialogComponent
  ],
  imports: [
    CommonModule,
    InvitesRoutingModule,
    ReactiveFormsModule,
    UiComponentsModule,
    TranslateModule,
    InvitesActionsModule
  ],
  exports: []
})
export class InvitesModule {
}
