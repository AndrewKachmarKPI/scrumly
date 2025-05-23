import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { HomeRoutingModule } from './home-routing.module';
import { MemberDashboardComponent } from './pages/member-dashboard/member-dashboard.component';
import { ScheduledEventsPageComponent } from '../events/page/scheduled-events/scheduled-events-page.component';
import { BadgeModule } from "primeng/badge";
import { Button } from "primeng/button";
import { ConfirmDialogModule } from "primeng/confirmdialog";
import { DividerModule } from "primeng/divider";
import { OrganizationsModule } from "../organizations/organizations.module";
import { CalendarModule } from "../callendar/calendar.module";
import {
  PrimeCalendarSchedulerModule
} from "../../../ui-components/scrumly-components/prime-callendar-scheduler/prime-calendar-scheduler.module";
import { SelectButtonModule } from "primeng/selectbutton";
import { UiComponentsModule } from "../../../ui-components/ui-components.module";
import { TranslatePipe } from "@ngx-translate/core";
import { ActivityLibraryModule } from "../activity-library/activity-library.module";
import { EventsModule } from "../events/events.module";
import { ActivityListDialogComponent } from './components/activity-list-dialog/activity-list-dialog.component';


@NgModule({
  declarations: [
    MemberDashboardComponent,
    ScheduledEventsPageComponent,
    ActivityListDialogComponent
  ],
    imports: [
        CommonModule,
        HomeRoutingModule,
        BadgeModule,
        Button,
        ConfirmDialogModule,
        DividerModule,
        OrganizationsModule,
        CalendarModule,
        PrimeCalendarSchedulerModule,
        SelectButtonModule,
        UiComponentsModule,
        TranslatePipe,
        ActivityLibraryModule,
        EventsModule
    ]
})
export class HomeModule { }
