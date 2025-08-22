import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './client-dashboard.component.html'
})
export class ClientDashboardComponent {
  userName = 'Youssef';
  userEmail = 'client@example.com';
  sidebarOpen = false;

  constructor(private router: Router) {}

  toggleSidebar() { this.sidebarOpen = !this.sidebarOpen; }
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    this.router.navigateByUrl('/login');
  }

}
