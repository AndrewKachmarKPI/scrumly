import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {PublicRoutingModule} from './public-routing.module';
import {HomeComponent} from './pages/home/home.component';
import {HeaderMenuComponent} from './components/header-menu/header-menu.component';
import {UiComponentsModule} from "../ui-components/ui-components.module";
import {TranslatePipe} from "@ngx-translate/core";
import {FooterMenuComponent} from './components/footer-menu/footer-menu.component';
import { PublicLayoutComponent } from './components/layout/public-layout.component';
import { PublicOutletComponent } from './components/public-outlet/public-outlet.component';
import { RegistrationComponent } from './pages/registration/registration.component';
import {ReactiveFormsModule} from "@angular/forms";
import {PrimeComponentsModule} from "../ui-components/scrumly-components/prime-components.module";


@NgModule({
  declarations: [
    HomeComponent,
    HeaderMenuComponent,
    FooterMenuComponent,
    PublicLayoutComponent,
    PublicOutletComponent,
    RegistrationComponent,
  ],
  exports: [
    HeaderMenuComponent
  ],
  imports: [
    CommonModule,
    PublicRoutingModule,
    UiComponentsModule,
    TranslatePipe,
    ReactiveFormsModule,
    PrimeComponentsModule
  ]
})
export class PublicModule {
}
