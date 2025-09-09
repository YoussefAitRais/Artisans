import { Routes, UrlTree } from '@angular/router';
import { inject } from '@angular/core';

// Pages/Layout
import { HomebannerComponent } from './homebanner/homebanner.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { CategoriesComponent } from './pages/public/categories/categories.component';

import { AdminDashboardComponent } from './layout/admin-dashboard/admin-dashboard.component';
import { AdminHomeComponent } from './pages/admin/home-admin/admin-home.component';
import { AdminUsersComponent } from './pages/admin/users/admin-users.component';
import { AdminCategoriesComponent } from './pages/admin/categories/admin-categories.component';
import { AdminReviewsComponent } from './pages/admin/reviews/admin-reviews.component';
import { AdminStatsComponent } from './pages/admin/stats/admin-stats.component';

import { ClientDashboardComponent } from './layout/client-dashboard/client-dashboard.component';
import { ClientHomeComponent } from './pages/client/home/client-home.component';
import { RequestsComponent } from './pages/client/requests/requests.component';
import { FavoritesComponent } from './pages/client/favorites/favorites.component';
import { SettingsComponent } from './pages/client/settings/settings.component';
import { ClientMessagingComponent } from './pages/client/messaging/client-messaging.component';
import { ClientEngagementsComponent } from './pages/client/engagements/client-engagements.component';

import { ArtisanDashboardComponent } from './layout/artisan-dashboard/artisan-dashboard.component';
import { ArtisanHomeComponent } from './pages/artisan/home-artisan/artisan-home.component';
import { InboxComponent } from './pages/artisan/inbox/inbox.component';
import { ArtisanQuotesComponent } from './pages/artisan/quotes/quotes.component';
import { ArtisanAvailabilityComponent } from './pages/artisan/availability/availability.component';
import { ArtisanPortfolioComponent } from './pages/artisan/portfolio/portfolio.component';
import { ArtisanReviewsComponent } from './pages/artisan/reviews/reviews.component';
import { ArtisanProfileComponent } from './pages/artisan/profile/profile.component';
import { ArtisanMessagingComponent } from './pages/artisan/messaging/artisan-messaging.component';
import { ArtisanEngagementsComponent } from './pages/artisan/engagements/artisan-engagements.component';

// Guards (functional)
import { authCanMatch } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';
import { loginRedirectGuard } from './guards/login-redirect.guard';

export const routes: Routes = [
  { path: '', component: HomebannerComponent, pathMatch: 'full' },

  // Login/Register (ما تدخلش ليهم واخا عندك session)
  { path: 'login',    component: LoginComponent,    canMatch: [loginRedirectGuard] },
  { path: 'register', component: RegisterComponent, canMatch: [loginRedirectGuard] },

  //{ path: 'login',    component: LoginComponent,    canMatch: [loginRedirectGuard] },
  //{ path: 'register', component: RegisterComponent, canMatch: [loginRedirectGuard] },

  // Public
  { path: 'categories', component: CategoriesComponent },

  // Admin area
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canMatch: [authCanMatch, roleGuard('ADMIN')],
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: AdminHomeComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'categories', component: AdminCategoriesComponent },
      { path: 'reviews', component: AdminReviewsComponent },
      { path: 'stats', component: AdminStatsComponent },
    ]
  },

  // Client area
  {
    path: 'client',
    component: ClientDashboardComponent,
    canMatch: [authCanMatch, roleGuard('CLIENT')],
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', component: ClientHomeComponent },
      { path: 'requests', component: RequestsComponent },
      { path: 'messages', component: ClientMessagingComponent },
      { path: 'engagements', component: ClientEngagementsComponent },
      { path: 'favorites', component: FavoritesComponent },
      { path: 'settings', component: SettingsComponent },
    ]
  },

  // Artisan area
  {
    path: 'artisan',
    component: ArtisanDashboardComponent,
    canMatch: [authCanMatch, roleGuard('ARTISAN')],
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home',         component: ArtisanHomeComponent },
      { path: 'requests',     component: InboxComponent },
      { path: 'quotes',       component: ArtisanQuotesComponent },
      { path: 'messages',     component: ArtisanMessagingComponent },
      { path: 'engagements',  component: ArtisanEngagementsComponent },
      { path: 'availability', component: ArtisanAvailabilityComponent },
      { path: 'portfolio',    component: ArtisanPortfolioComponent },
      { path: 'reviews',      component: ArtisanReviewsComponent },
      { path: 'profile',      component: ArtisanProfileComponent },
    ]
  },

  { path: '**', redirectTo: '' }
];
