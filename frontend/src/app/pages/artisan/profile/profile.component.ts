import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ArtisanApiService, ArtisanProfile } from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-profile',
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html'
})
export class ArtisanProfileComponent implements OnInit {
  private api = inject(ArtisanApiService);

  model: ArtisanProfile = { metier: '', localisation: '', description: '' };
  saving = false;

  ngOnInit(): void {
    this.api.getMyProfile().subscribe(p => (this.model = p));
  }

  /** Persist profile changes */
  save(): void {
    this.saving = true;
    this.api.updateMyProfile(this.model).subscribe({
      next: p => (this.model = p),
      complete: () => (this.saving = false)
    });
  }
}
