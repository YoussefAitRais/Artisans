import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';

interface Page<T> {
  content: T[];
  number: number;      // 0-based
  size: number;
  totalPages: number;
  totalElements: number;
}

interface ClientResponse {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
}

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-home.component.html'
})
export class AdminHomeComponent implements OnInit {
  private readonly API_BASE = 'http://localhost:8091';

  // إحصائيات (Signals)
  clients  = signal(0);
  artisans = signal(0);
  demandes = signal(0);
  avis     = signal(0);

  // لائحة آخر العملاء
  loading = signal(false);
  error   = signal<string>('');
  data    = signal<Page<ClientResponse> | null>(null);

  // pager صغير للجدول المصغر
  pageIdx = signal(0);
  size    = signal(5);
  totalPages = computed(() => this.data()?.totalPages ?? 1);

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadClients();
  }

  // لو عندك /api/admin/stats غادي يكمّل الأرقام، وإلاّ يعمر clients فقط من totalElements
  private loadStats(): void {
    this.http.get<{clients:number; artisans:number; demandes:number; avis:number}>(`${this.API_BASE}/api/admin/stats`)
      .subscribe({
        next: s => {
          this.clients.set(s.clients ?? 0);
          this.artisans.set(s.artisans ?? 0);
          this.demandes.set(s.demandes ?? 0);
          this.avis.set(s.avis ?? 0);
        },
        error: _ => {
        }
      });
  }

  private loadClients(): void {
    this.loading.set(true);
    this.error.set('');

    const params = new HttpParams()
      .set('page', this.pageIdx())
      .set('size', this.size());

    this.http.get<Page<ClientResponse>>(`${this.API_BASE}/api/admin/clients`, { params })
      .subscribe({
        next: (page) => {
          this.data.set(page);
          if (this.clients() === 0 && page.totalElements >= 0) {
            this.clients.set(page.totalElements);
          }
        },
        error: (err) => {
          const msg = err?.error?.message || err?.message || 'Erreur lors du chargement';
          this.error.set(msg);
          this.data.set({ content: [], number: 0, size: this.size(), totalPages: 1, totalElements: 0 });
        },
        complete: () => this.loading.set(false)
      });
  }

  prev(): void {
    const p = this.pageIdx();
    if (p > 0) { this.pageIdx.set(p - 1); this.loadClients(); }
  }
  next(): void {
    const p = this.pageIdx();
    if (p + 1 < this.totalPages()) { this.pageIdx.set(p + 1); this.loadClients(); }
  }

  trackById = (_: number, c: ClientResponse) => c.id;
}
