import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ActivityLibraryRoutingModule } from './activity-library-routing.module';
import { ToastModule } from "primeng/toast";
import { UiComponentsModule } from "../../../ui-components/ui-components.module";
import { ActivityLibrarySearchComponent } from './pages/activity-library-search/activity-library-search.component';
import { TemplateCardComponent } from './components/template-card/template-card.component';
import { TemplateBlockInfoComponent } from './components/template-block-info/template-block-info.component';
import { TranslatePipe } from "@ngx-translate/core";
import { CreateActivityTemplateComponent } from './pages/create-activity-template/create-activity-template.component';
import { TemplateConstructorComponent } from './components/contructor/template-constructor/template-constructor.component';
import { TemplateDetailsFormComponent } from './components/contructor/template-details-form/template-details-form.component';
import { ReactiveFormsModule } from "@angular/forms";
import { TemplateTagsComponent } from './components/contructor/template-tags/template-tags.component';
import { TemplateAgendaFormComponent } from './components/contructor/template-agenda-form/template-agenda-form.component';
import { SelectAgendaBlockDialogComponent } from './components/contructor/blocks/select-agenda-block-dialog/select-agenda-block-dialog.component';
import { CreateQuestionBlockComponent } from './components/contructor/blocks/create-question-block/create-question-block.component';
import { CreateReflectBlockComponent } from './components/contructor/blocks/create-reflect-block/create-reflect-block.component';
import { CreateActivityBlockComponent } from './components/contructor/blocks/create-activity-block/create-activity-block.component';
import { OrderAgendaBlocksDialogComponent } from './components/contructor/blocks/order-agenda-blocks-dialog/order-agenda-blocks-dialog.component';
import { ActivityTemplateDetailsComponent } from './pages/activity-template-details/activity-template-details.component';
import { TemplateDetailsCardComponent } from './components/template-details-card/template-details-card.component';
import { OrganizationsModule } from "../organizations/organizations.module";
import { CopyTemplateDialogComponent } from './dialogs/copy-template-dialog/copy-template-dialog.component';
import { CreateEstimateBlockComponent } from './components/contructor/blocks/create-estimate-block/create-estimate-block.component';
import { CreateItemsBoardBlockComponent } from './components/contructor/blocks/create-items-board-block/create-items-board-block.component';


@NgModule({
    declarations: [
        ActivityLibrarySearchComponent,
        TemplateCardComponent,
        TemplateBlockInfoComponent,
        CreateActivityTemplateComponent,
        TemplateConstructorComponent,
        TemplateDetailsFormComponent,
        TemplateTagsComponent,
        TemplateAgendaFormComponent,
        SelectAgendaBlockDialogComponent,
        CreateQuestionBlockComponent,
        CreateReflectBlockComponent,
        CreateActivityBlockComponent,
        OrderAgendaBlocksDialogComponent,
        ActivityTemplateDetailsComponent,
        TemplateDetailsCardComponent,
        CopyTemplateDialogComponent,
        CreateEstimateBlockComponent,
        CreateItemsBoardBlockComponent
    ],
    exports: [
        TemplateCardComponent,
        TemplateDetailsCardComponent
    ],
    imports: [
        CommonModule,
        ActivityLibraryRoutingModule,
        ToastModule,
        UiComponentsModule,
        TranslatePipe,
        ReactiveFormsModule,
        OrganizationsModule
    ]
})
export class ActivityLibraryModule { }
