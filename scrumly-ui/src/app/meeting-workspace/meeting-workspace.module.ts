import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MeetingWorkspaceRoutingModule } from './meeting-workspace-routing.module';
import { ConferenceModule } from "./conference/conference.module";
import { UiComponentsModule } from "../ui-components/ui-components.module";
import { ConferenceLayoutService } from './conference/service/conference-layout.service';

@NgModule({
  declarations: [
  ],
  imports: [
    CommonModule,
    MeetingWorkspaceRoutingModule,
    ConferenceModule,
    UiComponentsModule
  ],
  providers: [ConferenceLayoutService]
})
export class MeetingWorkspaceModule { }
