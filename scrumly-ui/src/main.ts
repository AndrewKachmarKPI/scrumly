import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { registerLicense } from '@syncfusion/ej2-base';
import { environment } from "./enviroments/enviroment";

registerLicense(environment.syncfussion_key);


platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));
