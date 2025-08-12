import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type ReqStatus = 'EN_ATTENTE' | 'ACCEPTEE' | 'REFUSEE' | 'TERMINEE';

interface ClientRequest {
  id: number;
  title: string;
  category: string;
  city: string;
  status: ReqStatus;
  createdAt: string; // ISO
  description: string;
}

@Component({
  selector: 'app-requests',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './requests.component.html'
})
export class RequestsComponent {
  // --- Mock data ---
  requests = signal<ClientRequest[]>([
    { id: 501, title: 'Réparation clim', category: 'Climatisation', city: 'Casablanca', status: 'EN_ATTENTE', createdAt: '2025-08-10', description: 'Clim fait du bruit et ne refroidit plus.' },
    { id: 502, title: 'Plomberie cuisine', category: 'Plomberie',    city: 'Rabat',      status: 'ACCEPTEE',   createdAt: '2025-08-07', description: 'Fuite sous l’évier, besoin d’un remplacement.' },
    { id: 503, title: 'Peinture salon',   category: 'Peinture',      city: 'Fès',        status: 'EN_ATTENTE', createdAt: '2025-08-01', description: 'Peinture murale couleur claire, 20 m².' },
    { id: 504, title: 'Panne électrique', category: 'Électricité',   city: 'Marrakech',  status: 'REFUSEE',    createdAt: '2025-07-28', description: 'Prises ne fonctionnent plus dans le salon.' },
    { id: 505, title: 'Entretien clim',   category: 'Climatisation', city: 'Casablanca', status: 'TERMINEE',   createdAt: '2025-07-15', description: 'Nettoyage et recharge gaz.' },
  ]);

  // --- Filters / sort / paging ---
  q = signal('');
  status = signal<ReqStatus | 'ALL'>('ALL');
  city = signal<'ALL' | string>('ALL');
  sortKey = signal<'createdAt' | 'title' | 'city' | 'status'>('createdAt');
  sortDir = signal<'asc' | 'desc'>('desc');

  page = signal(1);
  size = signal(5);

  cities = computed(() => {
    const set = new Set(this.requests().map(r => r.city));
    return Array.from(set).sort();
  });

  filtered = computed(() => {
    const text = this.q().toLowerCase();
    const st = this.status();
    const c = this.city();

    const arr = this.requests().filter(r => {
      const matchText = !text || [r.title, r.category, r.city, r.description].join(' ').toLowerCase().includes(text);
      const matchStatus = st === 'ALL' || r.status === st;
      const matchCity = c === 'ALL' || r.city === c;
      return matchText && matchStatus && matchCity;
    });

    const key = this.sortKey();
    const dir = this.sortDir();
    return arr.sort((a, b) => {
      const av = (a as any)[key];
      const bv = (b as any)[key];
      if (av < bv) return dir === 'asc' ? -1 : 1;
      if (av > bv) return dir === 'asc' ? 1 : -1;
      return 0;
    });
  });

  totalPages = computed(() => Math.max(1, Math.ceil(this.filtered().length / this.size())));
  paged = computed(() => {
    const start = (this.page() - 1) * this.size();
    return this.filtered().slice(start, start + this.size());
  });

  setSort(k: 'createdAt' | 'title' | 'city' | 'status') {
    if (this.sortKey() === k) this.sortDir.set(this.sortDir() === 'asc' ? 'desc' : 'asc');
    else { this.sortKey.set(k); this.sortDir.set('asc'); }
  }

  reset() {
    this.q.set('');
    this.status.set('ALL');
    this.city.set('ALL');
    this.sortKey.set('createdAt');
    this.sortDir.set('desc');
    this.page.set(1);
  }

  prev() { this.page.set(Math.max(1, this.page() - 1)); }
  next() { this.page.set(Math.min(this.totalPages(), this.page() + 1)); }
  trackById = (_: number, r: ClientRequest) => r.id;

  // modal
  show = signal(false);
  current = signal<ClientRequest | null>(null);
  open(r: ClientRequest) { this.current.set(r); this.show.set(true); }
  close() { this.show.set(false); }

  cancel(r: ClientRequest) {
    if (!confirm(`Annuler la demande #${r.id} ?`)) return;
    this.requests.update(list => list.map(x => x.id === r.id ? { ...x, status: 'REFUSEE' } : x));
  }
}
