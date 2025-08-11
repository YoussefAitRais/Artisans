import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

import 'preline';

if (typeof window !== 'undefined') {
  import('preline');
}
bootstrapApplication(AppComponent, appConfig)
  .catch(err => console.error(err));
