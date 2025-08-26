import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ArtisanApiService,
  ArtisanInboxItem,
  Page,
  OfferCreateRequest
} from '../../../services/api/artisan-api.service';

type InboxStatus =
  | 'PENDING' | 'RESPONDED' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';

@Component({
  standalone: true,
  selector: 'app-artisan-inbox',
  imports: [CommonModule, FormsModule],
  templateUrl: './inbox.component.html'
})
export class InboxComponent implements OnInit {
  // state
  loading = false;
  items: ArtisanInboxItem[] = [];

  // filters
  q = '';
  statusFilter: 'ALL' | 'PENDING' | 'RESPONDED' = 'PENDING';

  // paging
  page = 0;          // zero-based
  size = 10;
  totalPages = 1;
  totalElements = 0;

  // details
  showDetails = false;
  current: ArtisanInboxItem | null = null;

  // offer modal
  showOffer = false;
  sending = false;
  offer: { price: number | null; estimatedDays: number | null; message: string } = {
    price: null, estimatedDays: null, message: ''
  };

  constructor(private api: ArtisanApiService) {}

  ngOnInit(): void { this.load(); }

  // load list
  load() {
    this.loading = true;
    this.api.inbox(this.page, this.size, this.statusFilter).subscribe({
      next: (p: Page<ArtisanInboxItem>) => {
        const t = this.q.trim().toLowerCase();
        const base = p.content ?? [];
        this.items = t
          ? base.filter(i =>
            `${i.title} ${i.city ?? ''} ${i.description ?? ''}`
              .toLowerCase()
              .includes(t))
          : base;

        this.totalElements = p.totalElements ?? base.length;
        this.totalPages = p.totalPages || 1;
      },
      error: (e) => console.error('inbox load failed', e),
      complete: () => (this.loading = false),
    });
  }

  onSearch() { this.page = 0; this.load(); }
  reset()    { this.q = ''; this.statusFilter = 'PENDING'; this.page = 0; this.load(); }

  // paging
  prev() { if (this.page > 0) { this.page--; this.load(); } }
  next() { if (this.page + 1 < this.totalPages) { this.page++; this.load(); } }
  changeSize(s: number) { this.size = s; this.page = 0; this.load(); }
  trackById = (_: number, it: ArtisanInboxItem) => it.id;

  // details
  openDetails(it: ArtisanInboxItem) { this.current = it; this.showDetails = true; }
  closeDetails() { this.showDetails = false; }

  // offer
  openOffer(it: ArtisanInboxItem) {
    this.current = it;
    this.offer = {
      price: null,
      estimatedDays: null,
      message: `Bonjour,\nJe vous propose mes services pour "${it.title}".\nCordialement.`
    };
    this.showOffer = true;
  }
  closeOffer() { if (!this.sending) this.showOffer = false; }

  sendOffer() {
    if (!this.current) return;

    const body: OfferCreateRequest = {
      price: Number(this.offer.price || 0),
      message: this.offer.message?.trim() || undefined,
      estimatedDays: this.offer.estimatedDays && this.offer.estimatedDays > 0
        ? this.offer.estimatedDays : undefined
    };

    if (!body.price || body.price <= 0) {
      alert('Le prix est obligatoire et doit être > 0.');
      return;
    }

    this.sending = true;
    this.api.sendOffer(this.current.id, body).subscribe({
      next: () => {
        this.sending = false;
        this.showOffer = false;
        alert('Offre envoyée ✔️');
        this.load();
      },
      error: (e) => {
        this.sending = false;
        alert(e?.error?.message || e?.message || 'Erreur inattendue');
      }
    });
  }

  // badge classes
  badgeClass(s: InboxStatus) {
    return {
      'bg-amber-100 text-amber-700': s === 'PENDING',
      'bg-blue-100 text-blue-700' : s === 'RESPONDED',
      'bg-green-100 text-green-700': s === 'ACCEPTED',
      'bg-rose-100 text-rose-700' : s === 'REJECTED' || s === 'CANCELLED',
      'bg-gray-100 text-gray-700' : s === 'COMPLETED'
    };
  }
}
