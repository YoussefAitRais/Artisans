import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';

type NavItem = { label: string; path: string; icon: string; };

@Component({
  selector: 'app-artisan-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './artisan-dashboard.component.html'
})
export class ArtisanDashboardComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  sidebarOpen = false;

  nav: NavItem[] = [
    { label: 'Accueil',            path: '/artisan/home',          icon: '🏠' },
    { label: 'Demandes reçues',    path: '/artisan/requests',      icon: '📥' },
    { label: 'Mes devis',          path: '/artisan/quotes',        icon: '📄' },
    { label: 'Disponibilités',     path: '/artisan/availability',  icon: '🗓️' },
    { label: 'Portfolio',          path: '/artisan/portfolio',     icon: '🖼️' },
    { label: 'Avis',               path: '/artisan/reviews',       icon: '⭐' },
    { label: 'Profil',             path: '/artisan/profile',       icon: '👤' },
  ];

  toggleSidebar() { this.sidebarOpen = !this.sidebarOpen; }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
