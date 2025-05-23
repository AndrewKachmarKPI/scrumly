import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { EventsRoutingModule } from './events-routing.module';
import { ScheduleEventSidebarComponent } from './components/schedule-event-sidebar/schedule-event-sidebar.component';
import { UiComponentsModule } from "../../../ui-components/ui-components.module";
import { ReactiveFormsModule } from "@angular/forms";
import { OrganizationsModule } from "../organizations/organizations.module";
import { ActivityTemplateDropdownComponent } from './components/activity-template-dropdown/activity-template-dropdown.component';
import { TranslatePipe } from "@ngx-translate/core";
import { SelectRecurenceDialogComponent } from './components/select-recurence-dialog/select-recurence-dialog.component';
import { ScheduleCalendarEventDialogComponent } from './components/schedule-calendar-event-dialog/schedule-calendar-event-dialog.component';
import { ScheduledCalendarEventsComponent } from './components/scheduled-calendar-events/scheduled-calendar-events.component';
import { ScheduledEventsTableComponent } from './components/scheduled-events-table/scheduled-events-table.component';
import { SelectTimeSlotDialogComponent } from './components/select-time-slot-dialog/select-time-slot-dialog.component';


@NgModule({
  declarations: [
    ScheduleEventSidebarComponent,
    ActivityTemplateDropdownComponent,
    SelectRecurenceDialogComponent,
    ScheduleCalendarEventDialogComponent,
    ScheduledCalendarEventsComponent,
    ScheduledEventsTableComponent,
    SelectTimeSlotDialogComponent
  ],
    exports: [
        ScheduleEventSidebarComponent,
        ScheduledEventsTableComponent,
        ScheduledCalendarEventsComponent
    ],
  imports: [
    CommonModule,
    EventsRoutingModule,
    UiComponentsModule,
    ReactiveFormsModule,
    OrganizationsModule,
    TranslatePipe
  ]
})
export class EventsModule { }
