// src/app/app.routes.ts
import { Routes } from '@angular/router';

// صفحات عامّة
import { HomeComponent } from './home/home.component';
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

// Guards (نسخ CanMatch آمنة لـ SSR)
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';
import { loginRedirectGuard } from './guards/login-redirect.guard';

export const routes: Routes = [
  // --- عام ---
  { path: '', component: HomeComponent, pathMatch: 'full' },

  // هادو مسموحين فقط لغير المسجّلين:
  { path: 'login', component: LoginComponent, canMatch: [loginRedirectGuard] },
  { path: 'register', component: RegisterComponent, canMatch: [loginRedirectGuard] },

  // --- Client Dashboard ---
  {
    path: 'client',
    component: ClientDashboardComponent,
    canMatch: [authGuard, roleGuard('CLIENT')],
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: ClientHomeComponent },
      { path: 'requests', component: RequestsComponent },
      { path: 'favorites', component: FavoritesComponent },
      { path: 'settings', component: SettingsComponent },
    ]
  },

  // --- Admin Dashboard ---
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canMatch: [authGuard, roleGuard('ADMIN')],
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: AdminHomeComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'categories', component: AdminCategoriesComponent },
      { path: 'reviews', component: AdminReviewsComponent },
      { path: 'stats', component: AdminStatsComponent },
    ]
  },

  // باقي الحالات
  { path: '**', redirectTo: '' }
];
