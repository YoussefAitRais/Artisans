import { Routes } from '@angular/router';
import { Register } from './register/register.component';
import {LoginComponent} from "./login/login.component";

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'register',
    pathMatch: 'full'
  },
  {
    path: 'register',
    component: Register
  },
  {
  path: 'login',
  component: LoginComponent
  },
];
