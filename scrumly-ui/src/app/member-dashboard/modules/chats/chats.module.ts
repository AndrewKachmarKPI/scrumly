import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ChatsRoutingModule } from './chats-routing.module';
import { BacklogRoutingModule } from '../backlog/backlog-routing.module';
import { UiComponentsModule } from '../../../ui-components/ui-components.module';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    BacklogRoutingModule,
    UiComponentsModule,
  ]
})
export class ChatsModule { }
