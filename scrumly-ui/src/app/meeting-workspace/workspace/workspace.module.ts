import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { WorkspaceRoutingModule } from './workspace-routing.module';
import { WorkspacePageComponent } from './pages/workspace-page/workspace-page.component';
import { WorkspaceDashboardComponent } from './component/workspace-dashboard/workspace-dashboard.component';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PrimeComponentsModule } from '../../ui-components/scrumly-components/prime-components.module';
import { MessagesModule } from 'primeng/messages';
import { UiComponentsModule } from '../../ui-components/ui-components.module';
import { OrganizationsModule } from '../../member-dashboard/modules/organizations/organizations.module';
import {
  TemplateBlockNavigationComponent
} from './component/template-block-navigation/template-block-navigation.component';
import { TranslatePipe } from '@ngx-translate/core';
import {
  WorkspaceDashboardSidebarComponent
} from './component/workspace-dashboard-sidebar/workspace-dashboard-sidebar.component';
import {
  WorkspaceDashboardContentComponent
} from './component/workspace-dashboard-content/workspace-dashboard-content.component';
import {
  WorkspaceDashboardContentHeaderComponent
} from './component/workspace-dashboard-content-header/workspace-dashboard-content-header.component';
import { WorkspaceContentBlockComponent } from './component/workspace-content-block/workspace-content-block.component';
import { QuestionBlockComponent } from './component/workspace-content-block/question-block/question-block.component';
import { ReflectBlockComponent } from './component/workspace-content-block/reflect-block/reflect-block.component';
import { EstimateBlockComponent } from './component/workspace-content-block/estimate-block/estimate-block.component';
import { WebsocketActivityRoomService } from '../websocket/websocket-activity-room.service';
import { WorkspaceDashboardFacilitatorActionsComponent } from './component/workspace-dashboard-facilitator-actions/workspace-dashboard-facilitator-actions.component';
import { WorkspaceTimerComponent } from './component/workspace-timer/workspace-timer.component';
import { ReactiveFormsModule } from '@angular/forms';
import { SelectEstimateItemsComponent } from './component/workspace-content-block/estimate-block/select-estimate-items/select-estimate-items.component';
import { EstimateItemRowComponent } from './component/workspace-content-block/estimate-block/estimate-item-row/estimate-item-row.component';
import { EstimateItemWorkspaceComponent } from './component/workspace-content-block/estimate-block/estimate-item-workspace/estimate-item-workspace.component';
import { EstimateScaleDeckComponent } from './component/workspace-content-block/estimate-block/estimate-scale-deck/estimate-scale-deck.component';
import { EstimateCardComponent } from './component/workspace-content-block/estimate-block/estimate-card/estimate-card.component';
import { NgxSpinnerComponent } from 'ngx-spinner';
import { ConferenceKickedComponent } from './pages/conference-kicked/conference-kicked.component';
import { WorkspaceReportComponent } from './component/workspace-report/workspace-report.component';
import { EstimateBlockReportComponent } from './component/workspace-report/estimate-block-report/estimate-block-report.component';
import { ReflectBlockReportComponent } from './component/workspace-report/reflect-block-report/reflect-block-report.component';
import { QuestionBlockReportComponent } from './component/workspace-report/question-block-report/question-block-report.component';
import { WorkspaceMeetingNotesSidebarComponent } from './component/workspace-meeting-notes-sidebar/workspace-meeting-notes-sidebar.component';
import { ItemBoardBlockComponent } from './component/workspace-content-block/item-board-block/item-board-block.component';
import { SelectBacklogItemsDialogComponent } from './component/workspace-content-block/item-board-block/select-backlog-items-dialog/select-backlog-items-dialog.component';
import { ItemBoardBlockReportComponent } from './component/workspace-report/item-board-block-report/item-board-block-report.component';
import { SyncBlockDialogComponent } from './component/sync-block-dialog/sync-block-dialog.component';
import { TeamCapacityTableComponent } from './component/workspace-content-block/item-board-block/team-capacity-table/team-capacity-table.component';


@NgModule({
  declarations: [
    WorkspacePageComponent,
    WorkspaceDashboardComponent,
    TemplateBlockNavigationComponent,
    WorkspaceDashboardSidebarComponent,
    WorkspaceDashboardContentComponent,
    WorkspaceDashboardContentHeaderComponent,
    WorkspaceContentBlockComponent,
    QuestionBlockComponent,
    ReflectBlockComponent,
    EstimateBlockComponent,
    WorkspaceDashboardFacilitatorActionsComponent,
    WorkspaceTimerComponent,
    SelectEstimateItemsComponent,
    EstimateItemRowComponent,
    EstimateItemWorkspaceComponent,
    EstimateScaleDeckComponent,
    EstimateCardComponent,
    ConferenceKickedComponent,
    WorkspaceReportComponent,
    EstimateBlockReportComponent,
    ReflectBlockReportComponent,
    QuestionBlockReportComponent,
    WorkspaceMeetingNotesSidebarComponent,
    ItemBoardBlockComponent,
    SelectBacklogItemsDialogComponent,
    ItemBoardBlockReportComponent,
    SyncBlockDialogComponent,
    TeamCapacityTableComponent
  ],
  exports: [
    WorkspaceDashboardComponent,
    EstimateItemRowComponent
  ],
    imports: [
        CommonModule,
        WorkspaceRoutingModule,
        ProgressSpinnerModule,
        PrimeComponentsModule,
        MessagesModule,
        UiComponentsModule,
        OrganizationsModule,
        TranslatePipe,
        ReactiveFormsModule,
        NgxSpinnerComponent
    ],
  providers: [ WebsocketActivityRoomService ]
})
export class WorkspaceModule {
}
