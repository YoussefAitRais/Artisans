// src/app/app.routes.ts
import { Routes } from '@angular/router';

// --- Public pages
import { HomebannerComponent } from './homebanner/homebanner.component';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';

// --- Admin
import { AdminDashboardComponent } from './layout/admin-dashboard/admin-dashboard.component';
import { AdminHomeComponent } from './pages/admin/home-admin/admin-home.component';
import { AdminUsersComponent } from './pages/admin/users/admin-users.component';
import { AdminCategoriesComponent } from './pages/admin/categories/admin-categories.component';
import { AdminReviewsComponent } from './pages/admin/reviews/admin-reviews.component';
import { AdminStatsComponent } from './pages/admin/stats/admin-stats.component';

// --- Client
import { ClientDashboardComponent } from './layout/client-dashboard/client-dashboard.component';
import { ClientHomeComponent } from './pages/client/home/client-home.component';
import { RequestsComponent } from './pages/client/requests/requests.component';
import { FavoritesComponent } from './pages/client/favorites/favorites.component';
import { SettingsComponent } from './pages/client/settings/settings.component';

// --- Artisan
import { ArtisanDashboardComponent } from './layout/artisan-dashboard/artisan-dashboard.component';
import { ArtisanHomeComponent } from './pages/artisan/home-artisan/artisan-home.component';
import { ArtisanRequestsComponent } from './pages/artisan/requests/requests.component';
import { ArtisanQuotesComponent } from './pages/artisan/quotes/quotes.component';
import { ArtisanAvailabilityComponent } from './pages/artisan/availability/availability.component';
import { ArtisanPortfolioComponent } from './pages/artisan/portfolio/portfolio.component';
import { ArtisanReviewsComponent } from './pages/artisan/reviews/reviews.component';
import { ArtisanProfileComponent } from './pages/artisan/profile/profile.component';

// --- Guards
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';
import { loginRedirectGuard } from './guards/login-redirect.guard';

export const routes: Routes = [
  // ===== Public =====
  { path: '', component: HomebannerComponent, pathMatch: 'full' },
  { path: 'login', component: LoginComponent, canMatch: [loginRedirectGuard] },
  { path: 'register', component: RegisterComponent, canMatch: [loginRedirectGuard] },

  // ===== Admin Dashboard =====
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canMatch: [authGuard, roleGuard('ADMIN')],
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home',        component: AdminHomeComponent },
      { path: 'users',       component: AdminUsersComponent },
      { path: 'categories',  component: AdminCategoriesComponent },
      { path: 'reviews',     component: AdminReviewsComponent },
      { path: 'stats',       component: AdminStatsComponent },
    ]
  },

  // ===== Client Dashboard =====
  {
    path: 'client',
    component: ClientDashboardComponent,
    canMatch: [authGuard, roleGuard('CLIENT')],
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home',       component: ClientHomeComponent },
      { path: 'requests',   component: RequestsComponent },
      { path: 'favorites',  component: FavoritesComponent },
      { path: 'settings',   component: SettingsComponent },
    ]
  },

  // ===== Artisan Dashboard =====
  {
    path: 'artisan',
    component: ArtisanDashboardComponent,
    canMatch: [authGuard, roleGuard('ARTISAN')],
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home',         component: ArtisanHomeComponent },
      { path: 'requests',     component: ArtisanRequestsComponent },
      { path: 'quotes',       component: ArtisanQuotesComponent },
      { path: 'availability', component: ArtisanAvailabilityComponent },
      { path: 'portfolio',    component: ArtisanPortfolioComponent },
      { path: 'reviews',      component: ArtisanReviewsComponent },
      { path: 'profile',      component: ArtisanProfileComponent },
    ]
  },

  // ===== Fallback =====
  { path: '**', redirectTo: '' }
];
