import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ArtisanApiService,
  ArtisanProfile
} from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-profile',
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html'
})
export class ArtisanProfileComponent implements OnInit {
  private api = inject(ArtisanApiService);

  model: ArtisanProfile = { metier: '', localisation: '', description: '' };

  ngOnInit() {
    this.api.getMyProfile().subscribe({
      next: (p: ArtisanProfile) => this.model = { ...this.model, ...p }
    });
  }

  save() {
    this.api.updateMyProfile(this.model).subscribe({
      next: () => alert('Profil enregistré'),
      error: (e: any) => alert(e?.error?.message || 'Erreur')
    });
  }
}
