import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-home',
  standalone: true,
  templateUrl: './admin-home.component.html'
})
export class AdminHomeComponent {
  clients = 124;
  artisans = 58;
  demandes = 340;
  avis = 92;
}
