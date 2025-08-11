import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { AdminDashboardComponent } from './layout/admin-dashboard/admin-dashboard.component';
import { AdminHomeComponent } from './pages/admin/home/admin-home.component';
import { AdminUsersComponent } from './pages/admin/users/admin-users.component';
import { AdminCategoriesComponent } from './pages/admin/categories/admin-categories.component';
import { AdminReviewsComponent } from './pages/admin/reviews/admin-reviews.component';
import { AdminStatsComponent } from './pages/admin/stats/admin-stats.component';

export const routes: Routes = [
  // Public
  { path: '', component: HomeComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },

  // Admin layout + children
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

  // Fallback
  { path: '**', redirectTo: '' }
];
