import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IntegrationRoutingModule } from './integration-routing.module';
import { GoogleCalendarCallbackComponent } from './pages/google-calendar-callback/google-calendar-callback.component';
import { IntegrationService } from "./services/integration.service";
import {
  IntegrationServicesListComponent
} from './pages/integration-services-list/integration-services-list.component';
import { TranslateModule } from "@ngx-translate/core";
import { UiComponentsModule } from "../../../ui-components/ui-components.module";
import { JiraCloudCallbackComponent } from './pages/jira-cloud-callback/jira-cloud-callback.component';


@NgModule({
  declarations: [
    GoogleCalendarCallbackComponent,
    IntegrationServicesListComponent,
    JiraCloudCallbackComponent
  ],
  imports: [
    CommonModule,
    IntegrationRoutingModule,
    TranslateModule,
    UiComponentsModule
  ],
  exports: [
    IntegrationServicesListComponent
  ],
  providers: [IntegrationService]
})
export class IntegrationModule {
}
