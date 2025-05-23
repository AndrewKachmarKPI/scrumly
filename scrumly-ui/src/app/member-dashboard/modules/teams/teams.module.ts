import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TeamsRoutingModule } from './teams-routing.module';
import { MyTeamsComponent } from './pages/my-teams/my-teams.component';
import { UiComponentsModule } from '../../../ui-components/ui-components.module';
import { MyTeamComponent } from './pages/my-team/my-team.component';
import { TranslatePipe } from '@ngx-translate/core';
import { EventsModule } from '../events/events.module';
import { OrganizationsModule } from '../organizations/organizations.module';


@NgModule({
  declarations: [
    MyTeamsComponent,
    MyTeamComponent
  ],
    imports: [
        CommonModule,
        TeamsRoutingModule,
        UiComponentsModule,
        TranslatePipe,
        EventsModule,
        OrganizationsModule,
    ]
})
export class TeamsModule { }
