import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ArtisanApiService,
  Page,
  ArtisanInboxItem,
  InboxStatus,
} from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-inbox',
  templateUrl: './inbox.component.html',
  imports: [CommonModule, FormsModule],
})
export class InboxComponent implements OnInit {
  private api = inject(ArtisanApiService);

  q = '';
  statusFilter: InboxStatus = 'ALL';

  page = 0;
  size = 10;
  totalPages = 0;
  total = 0;

  loading = false;
  error = '';
  list: ArtisanInboxItem[] = [];

  showDetails = false;
  current: ArtisanInboxItem | null = null;

  replyingForId: number | null = null;

  // ==> اسم المتغير موافق للقالب
  offerModel = { price: 0, estimatedDays: 1, message: '' };

  ngOnInit(): void { this.load(); }

  private load(): void {
    this.loading = true; this.error = '';
    this.api.inbox(this.page, this.size, this.statusFilter, this.q || undefined).subscribe({
      next: (p: Page<ArtisanInboxItem>) => { this.list = p.content; this.total = p.totalElements; this.totalPages = p.totalPages; },
      error: () => (this.error = 'Une erreur est survenue.'),
      complete: () => (this.loading = false),
    });
  }

  onSearch(){ this.page = 0; this.load(); }
  reset(){ this.q = ''; this.statusFilter = 'ALL'; this.page = 0; this.size = 10; this.load(); }
  prev(){ if (this.page > 0) { this.page--; this.load(); } }
  next(){ if (this.page + 1 < this.totalPages) { this.page++; this.load(); } }
  changeSize(s: number){ this.size = s; this.page = 0; this.load(); }
  trackById = (_: number, x: ArtisanInboxItem) => x.id;

  badgeClass(s: 'PENDING' | 'RESPONDED'){ return s === 'RESPONDED' ? 'bg-emerald-700/40 text-emerald-200' : 'bg-amber-700/40 text-amber-200'; }

  openDetails(r: ArtisanInboxItem){ this.current = r; this.showDetails = true; }
  closeDetails(){ this.current = null; this.showDetails = false; }

  canReply(r: ArtisanInboxItem){ return r.status !== 'RESPONDED'; }
  beginReply(r: ArtisanInboxItem){ this.replyingForId = r.id; this.offerModel = { price: 0, estimatedDays: 1, message: '' }; }

  sendOffer(): void {
    if (this.replyingForId == null) return;
    this.api.createQuoteForRequest(this.replyingForId, {
      price: this.offerModel.price,
      estimatedDays: this.offerModel.estimatedDays,
      message: this.offerModel.message,
    }).subscribe(() => {
      this.replyingForId = null;
      this.offerModel = { price: 0, estimatedDays: 1, message: '' };
      this.load();
    });
  }
}
