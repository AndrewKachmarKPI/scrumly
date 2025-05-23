import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CalendarRoutingModule } from './calendar-routing.module';
import { EventsCalendarComponent } from './events-calendar/events-calendar.component';
import { UiComponentsModule } from "../../../ui-components/ui-components.module";
import {
  PrimeCalendarSchedulerModule
} from "../../../ui-components/scrumly-components/prime-callendar-scheduler/prime-calendar-scheduler.module";


@NgModule({
  declarations: [
    EventsCalendarComponent
  ],
  exports: [
    EventsCalendarComponent
  ],
  imports: [
    CommonModule,
    CalendarRoutingModule,
    UiComponentsModule,
  ]
})
export class CalendarModule {
}
