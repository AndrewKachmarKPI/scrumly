import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { InvitesActionsRoutingModule } from './invites-actions-routing.module';
import { InvitesActionsOutletComponent } from './layout/invites-actions-outlet/invites-actions-outlet.component';
import { AcceptInviteFormComponent } from "./pages/accept-invite-form/accept-invite-form.component";
import { ToastModule } from "primeng/toast";
import { Button, ButtonDirective } from "primeng/button";
import { PrimeComponentsModule } from "../../../../ui-components/scrumly-components/prime-components.module";
import { TooltipModule } from "primeng/tooltip";
import { PaginatorModule } from "primeng/paginator";
import { Ripple } from "primeng/ripple";
import { TranslatePipe } from "@ngx-translate/core";
import { OrganizationsModule } from "../../organizations/organizations.module";
import { RadioButtonModule } from "primeng/radiobutton";


@NgModule({
  declarations: [
    InvitesActionsOutletComponent,
    AcceptInviteFormComponent
  ],
    imports: [
        CommonModule,
        InvitesActionsRoutingModule,
        ToastModule,
        Button,
        PrimeComponentsModule,
        TooltipModule,
        ButtonDirective,
        PaginatorModule,
        Ripple,
        TranslatePipe,
        OrganizationsModule,
        RadioButtonModule
    ]
})
export class InvitesActionsModule { }
