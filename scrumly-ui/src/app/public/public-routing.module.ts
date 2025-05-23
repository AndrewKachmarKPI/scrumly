import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./pages/home/home.component";
import {PublicOutletComponent} from "./components/public-outlet/public-outlet.component";
import {RegistrationComponent} from "./pages/registration/registration.component";

const routes: Routes = [
  {
    path: '',
    component: PublicOutletComponent,
    children: [
      {
        path: '',
        component: HomeComponent
      }
    ]
  },
  {
    path: 'register',
    component: RegistrationComponent
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PublicRoutingModule {
}
