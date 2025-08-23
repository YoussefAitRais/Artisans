import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';

interface Page<T> {
  content: T[];
  number: number;
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
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html'
})
export class AdminUsersComponent implements OnInit {
  private readonly API_BASE = 'http://localhost:8091';

  q       = signal('');
  page    = signal(1);   // 1-based for UI
  pageSize = signal(10);

  loading = signal(false);
  error   = signal<string>('');
  data    = signal<Page<ClientResponse> | null>(null);

  totalPages = computed(() => this.data()?.totalPages ?? 1);

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.load();
  }

  search(): void {
    this.page.set(1);
    this.load();
  }

  prev(): void {
    if (this.page() > 1) { this.page.set(this.page() - 1); this.load(); }
  }
  next(): void {
    if (this.page() < this.totalPages()) { this.page.set(this.page() + 1); this.load(); }
  }

  trackById = (_: number, c: ClientResponse) => c.id;

  private load(): void {
    this.loading.set(true);
    this.error.set('');

    const params = new HttpParams()
      .set('page', this.page() - 1) // backend 0-based
      .set('size', this.pageSize())
      .set('q', this.q().trim());

    this.http.get<Page<ClientResponse>>(`${this.API_BASE}/api/admin/clients`, { params })
      .subscribe({
        next: (page) => this.data.set(page),
        error: (err) => {
          const msg = err?.error?.message || err?.message || 'Erreur lors du chargement';
          this.error.set(msg);
          this.data.set({ content: [], number: 0, size: this.pageSize(), totalPages: 1, totalElements: 0 });
        },
        complete: () => this.loading.set(false)
      });
  }
}
