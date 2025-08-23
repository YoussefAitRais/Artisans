import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArtisanApiService, ArtisanRequest } from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-requests',
  imports: [CommonModule],
  templateUrl: './requests.component.html'
})
export class ArtisanRequestsComponent implements OnInit {
  private api = inject(ArtisanApiService);

  rows: ArtisanRequest[] = [];
  total = 0;
  loading = false;

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.api.getRequests({ page: 0, size: 20 }).subscribe({
      next: p => { this.rows = p.content; this.total = p.totalElements; },
      error: () => {},
      complete: () => this.loading = false
    });
  }
}
