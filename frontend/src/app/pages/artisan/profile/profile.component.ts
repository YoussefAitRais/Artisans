import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'app-artisan-profile',
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html'
})
export class ArtisanProfileComponent {
  model = { metier: '', localisation: '', description: '' };
  save() { /* TODO: call API */ alert('Profil enregistré (mock)'); }
}
