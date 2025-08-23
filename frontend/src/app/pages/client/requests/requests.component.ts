import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  CategoryApi,
  CategoryDto,
  ClientApi,
  Page,
  ServiceRequestResponse
} from '../../../services/api/client-api.service';

type UiStatus = 'ALL' | 'PENDING' | 'RESPONDED' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';

@Component({
  selector: 'app-requests',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './requests.component.html'
})
export class RequestsComponent implements OnInit {
  // paging/sort
  page = signal(1);
  size = signal(5);
  sortKey = signal<'createdAt' | 'title' | 'city' | 'status'>('createdAt');
  sortDir = signal<'asc' | 'desc'>('desc');

  // filters (client-side)
  q = signal('');
  status = signal<UiStatus>('ALL');
  city = signal<'ALL' | string>('ALL');

  // data
  pageData = signal<Page<ServiceRequestResponse> | null>(null);
  list = signal<ServiceRequestResponse[]>([]);

  // categories map
  categories = signal<CategoryDto[]>([]);
  private nameById = new Map<number, string>();

  // modal
  show = signal(false);
  current = signal<ServiceRequestResponse | null>(null);

  constructor(private api: ClientApi, private catApi: CategoryApi) {}

  ngOnInit(): void {
    this.catApi.getAll(0, 200, 'name,asc').subscribe({
      next: (p) => {
        this.categories.set(p.content);
        this.nameById.clear();
        p.content.forEach(c => this.nameById.set(c.id, c.name));
      }
    });
    this.load();
  }

  load() {
    const sort = `${this.sortKey()},${this.sortDir()}`;
    this.api.myRequests(this.page() - 1, this.size(), sort).subscribe({
      next: (res) => {
        this.pageData.set(res);
        this.list.set(res.content);
      }
    });
  }

  cities = computed(() => {
    const s = new Set(this.list().map(r => r.city || '').filter(Boolean));
    return Array.from(s).sort();
  });

  filtered = computed(() => {
    const text = this.q().toLowerCase();
    const st = this.status();
    const c = this.city();

    return this.list().filter(r => {
      const matchText = !text || [r.title, r.city, r.description].join(' ').toLowerCase().includes(text);
      const matchStatus = st === 'ALL' || r.status === st;
      const matchCity = c === 'ALL' || (r.city || '').toLowerCase() === c.toLowerCase();
      return matchText && matchStatus && matchCity;
    });
  });

  totalPages = computed(() => this.pageData()?.totalPages ?? 1);
  paged = computed(() => this.filtered());

  setSort(k: 'createdAt' | 'title' | 'city' | 'status') {
    if (this.sortKey() === k) this.sortDir.set(this.sortDir() === 'asc' ? 'desc' : 'asc');
    else { this.sortKey.set(k); this.sortDir.set('asc'); }
    this.page.set(1);
    this.load();
  }

  reset() {
    this.q.set('');
    this.status.set('ALL');
    this.city.set('ALL');
    this.sortKey.set('createdAt');
    this.sortDir.set('desc');
    this.page.set(1);
    this.load();
  }

  prev() { if (this.page() > 1) { this.page.set(this.page() - 1); this.load(); } }
  next() { if (this.page() < this.totalPages()) { this.page.set(this.page() + 1); this.load(); } }
  trackById = (_: number, r: ServiceRequestResponse) => r.id;

  open(r: ServiceRequestResponse) { this.current.set(r); this.show.set(true); }
  close() { this.show.set(false); }

  cancel(r: ServiceRequestResponse) {
    if (!confirm(`Annuler la demande #${r.id} ?`)) return;
    this.api.cancelRequest(r.id).subscribe({ next: () => this.load() });
  }

  delete(r: ServiceRequestResponse) {
    if (!confirm(`Supprimer la demande #${r.id} ?`)) return;
    this.api.deleteRequest(r.id).subscribe({ next: () => this.load() });
  }

  categoryName(id?: number | null) {
    if (!id) return '-';
    return this.nameById.get(id) ?? `#${id}`;
    // إلى بغيتي دقة أكثر، نقدر نعمل call منفصل إلا ماكانش فالماب
  }

  badgeClass(s: string) {
    return {
      'bg-amber-100 text-amber-700': s === 'PENDING',
      'bg-green-100 text-green-700': s === 'ACCEPTED',
      'bg-rose-100 text-rose-700' : s === 'REJECTED' || s === 'CANCELLED',
      'bg-blue-100 text-blue-700' : s === 'COMPLETED' || s === 'RESPONDED'
    };
  }

  statusText(s: string) {
    switch (s) {
      case 'PENDING':   return 'En attente';
      case 'RESPONDED': return 'Répondue';
      case 'ACCEPTED':  return 'Acceptée';
      case 'REJECTED':  return 'Refusée';
      case 'CANCELLED': return 'Annulée';
      case 'COMPLETED': return 'Terminée';
      default:          return s;
    }
  }
}
