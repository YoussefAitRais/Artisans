import { Routes } from '@angular/router';

import { HomeComponent } from './home/home.component';

// Auth
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';

// Admin
import { AdminDashboardComponent } from './layout/admin-dashboard/admin-dashboard.component';
import { AdminHomeComponent } from './pages/admin/home/admin-home.component';
import { AdminUsersComponent } from './pages/admin/users/admin-users.component';
import { AdminCategoriesComponent } from './pages/admin/categories/admin-categories.component';
import { AdminReviewsComponent } from './pages/admin/reviews/admin-reviews.component';
import { AdminStatsComponent } from './pages/admin/stats/admin-stats.component';

// Client
import { ClientDashboardComponent } from './layout/client-dashboard/client-dashboard.component';
import { ClientHomeComponent } from './pages/client/home/client-home.component';
import { RequestsComponent } from './pages/client/requests/requests.component';
import { FavoritesComponent } from './pages/client/favorites/favorites.component';
import { SettingsComponent } from './pages/client/settings/settings.component';

export const routes: Routes = [
  // Public
  { path: '', component: HomeComponent, pathMatch: 'full' },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },

  // Admin
  {
    path: 'admin',
    component: AdminDashboardComponent,
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: AdminHomeComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'categories', component: AdminCategoriesComponent },
      { path: 'reviews', component: AdminReviewsComponent },
      { path: 'stats', component: AdminStatsComponent },
    ]
  },

  // Client
  {
    path: 'client',
    component: ClientDashboardComponent,
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: ClientHomeComponent },
      { path: 'requests', component: RequestsComponent },
      { path: 'favorites', component: FavoritesComponent },
      { path: 'settings', component: SettingsComponent },
    ]
  },

  { path: '**', redirectTo: '' }
];
