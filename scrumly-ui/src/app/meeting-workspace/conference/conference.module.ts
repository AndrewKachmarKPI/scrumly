import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { environment } from '../../../enviroments/enviroment';
import { OpenViduAngularConfig, OpenViduAngularModule } from 'openvidu-angular';
import {
  PrimeOvVideoConferenceComponent
} from './components/prime-ov-videconference/prime-ov-video-conference.component';
import { UiComponentsModule } from '../../ui-components/ui-components.module';
import { ConferenceRoutingModule } from './conference-routing.module';
import { JoinConferencePageComponent } from './pages/join-conference-page/join-conference-page.component';
import { ConferenceDisconnectedComponent } from './pages/conference-disconnected/conference-disconnected.component';
import {
  PreJoinVideoConfigureComponent
} from './components/pre-join-video-configure/pre-join-video-configure.component';
import { WebsocketModule } from '../websocket/websocket.module';
import { ActivityLibraryModule } from '../../member-dashboard/modules/activity-library/activity-library.module';
import { PrimeOvVideoLayoutComponent } from './components/prime-ov-video-layout/prime-ov-video-layout.component';
import { PrimeOvStreamComponent } from './components/prime-ov-stream/prime-ov-stream.component';
import { WorkspaceModule } from '../workspace/workspace.module';
import { WebsocketConferenceRoomService } from '../websocket/websocket-conference-room.service';

const openViduConfig: OpenViduAngularConfig = {
  production: environment.production
};


@NgModule({
  declarations: [
    PrimeOvVideoConferenceComponent,
    JoinConferencePageComponent,
    ConferenceDisconnectedComponent,
    PreJoinVideoConfigureComponent,
    PrimeOvVideoLayoutComponent,
    PrimeOvStreamComponent
  ],
  imports: [
    CommonModule,
    UiComponentsModule,
    ConferenceRoutingModule,
    OpenViduAngularModule.forRoot(openViduConfig),
    WebsocketModule.config({
      url: environment.WS.conference_service
    }),
    ActivityLibraryModule,
    WorkspaceModule
  ],
  providers: [ WebsocketConferenceRoomService ]
})
export class ConferenceModule {
}
