import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import {NavbarComponent} from "./navbar/navbar.component";

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: '', component: NavbarComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent }
];
