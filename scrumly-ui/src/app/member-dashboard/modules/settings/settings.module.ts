import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SettingsRoutingModule } from './settings-routing.module';
import { MemberProfileComponent } from './pages/member-profile/member-profile.component';
import { MemberIntegrationsComponent } from './pages/member-integrations/member-integrations.component';
import { PrimeComponentsModule } from "../../../ui-components/scrumly-components/prime-components.module";
import { PrimeLibComponentsModule } from "../../../ui-components/prime-lib-components/prime-lib-components.module";
import { TranslateModule } from "@ngx-translate/core";
import { ReactiveFormsModule } from "@angular/forms";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { MemberInvitesComponent } from './pages/member-invites/member-invites.component';
import { OrganizationsModule } from "../organizations/organizations.module";
import { IntegrationModule } from "../integration/integration.module";


@NgModule({
  declarations: [
    MemberProfileComponent,
    MemberIntegrationsComponent,
    MemberInvitesComponent
  ],
    imports: [
        CommonModule,
        SettingsRoutingModule,
        TranslateModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        PrimeComponentsModule,
        PrimeLibComponentsModule,
        OrganizationsModule,
        IntegrationModule,
    ]
})
export class SettingsModule {
}
