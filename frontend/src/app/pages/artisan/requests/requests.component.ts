import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ArtisanApiService,
  ArtisanInboxItem,
  Page,
  InboxStatus
} from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-requests',
  imports: [CommonModule, FormsModule],
  templateUrl: './requests.component.html'
})
export class ArtisanRequestsComponent implements OnInit {
  private api = inject(ArtisanApiService);

  rows: ArtisanInboxItem[] = [];
  loading = false;
  error = '';

  // filters
  q = '';
  status: InboxStatus = 'ALL';

  // paging
  page = 0;
  size = 10;
  total = 0;
  get totalPages() { return Math.max(1, Math.ceil(this.total / this.size)); }

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.error = '';
    this.api.inbox(this.page, this.size, this.status, this.q).subscribe({
      next: (p: Page<ArtisanInboxItem>) => {
        this.rows = p.content ?? [];
        this.total = p.totalElements ?? this.rows.length;
      },
      error: (e) => this.error = e?.error?.message || 'Unexpected error',
      complete: () => this.loading = false
    });
  }

  onSearch() { this.page = 0; this.load(); }
  reset() { this.q = ''; this.status = 'ALL'; this.page = 0; this.load(); }
  prev() { if (this.page > 0) { this.page--; this.load(); } }
  next() { if (this.page + 1 < this.totalPages) { this.page++; this.load(); } }
  changeSize(s: number) { this.size = +s; this.page = 0; this.load(); }

  trackById = (_: number, r: ArtisanInboxItem) => r.id;

  badgeClass(s: ArtisanInboxItem['status']) {
    return {
      'bg-amber-100 text-amber-700': s === 'PENDING',
      'bg-sky-100 text-sky-700'   : s === 'RESPONDED',
    };
  }
}
