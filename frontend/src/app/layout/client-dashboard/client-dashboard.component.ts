import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
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

  constructor(private router: Router, private clientApi: ClientApi) {}

  ngOnInit(): void {
    this.clientApi.getMe().subscribe({
      next: (me: ClientResponse) => {
        this.userName = `${me.nom}${me.prenom ? ' ' + me.prenom : ''}`;
        this.userEmail = me.email;
      },
      error: () => {
        // لا توكن/expired → رجع للّوجين
        this.router.navigateByUrl('/login');
      }
    });
  }

  toggleSidebar() { this.sidebarOpen = !this.sidebarOpen; }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    this.router.navigateByUrl('/login');
  }
}
