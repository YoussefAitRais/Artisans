import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { ClientApi, ClientResponse } from '../../services/api/client-api.service';

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './client-dashboard.component.html'
})
export class ClientDashboardComponent implements OnInit {
  userName = '';
  userEmail = '';
  sidebarOpen = false;

  constructor(
    private router: Router, 
    private clientApi: ClientApi,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    this.clientApi.getMe().subscribe({
      next: (me: ClientResponse) => {
        this.userName = `${me.nom}${me.prenom ? ' ' + me.prenom : ''}`;
        this.userEmail = me.email;
      },
      error: () => this.router.navigateByUrl('/login')
    });
  }

  toggleSidebar() { this.sidebarOpen = !this.sidebarOpen; }

  logout() {
    // Check if we're in browser environment before accessing localStorage
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token');
      localStorage.removeItem('role');
    }
    this.router.navigateByUrl('/login');
  }
}
