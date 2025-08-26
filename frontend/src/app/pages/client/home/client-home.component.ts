import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ArtisanApiService,
  ArtisanDto,
  Page as ArtisanPage
} from '../../../services/api/artisan-api.service';
import {
  ClientApi,
  ServiceRequestCreateRequest
} from '../../../services/api/client-api.service';
import { CategoryApiService } from '../../../services/api/category-api.service';

type Card = {
  id: number;
  name: string;
  city: string;
  image: string;
  rating: number;
  metier: string;
  categoryId?: number;
};

type FormModel = {
  title: string;
  city: string;
  desiredDate: string; // dd/MM/yyyy أو yyyy-MM-dd
  description: string;
  categoryId: number | null;
};

@Component({
  standalone: true,
  selector: 'app-client-home',
  imports: [CommonModule, FormsModule],
  templateUrl: './client-home.component.html'
})
export class ClientHomeComponent implements OnInit {
  loading = false;
  total = 0;
  cards: Card[] = [];

  keyword = '';
  city = '';
  categoryId: number | null = null;

  // الطلب (Modal)
  showModal = false;
  sending = false;
  errorMsg = '';
  categories: { id: number; name: string }[] = [];

  form: FormModel = {
    title: '',
    city: '',
    desiredDate: '',
    description: '',
    categoryId: null
  };

  constructor(
    private artisanApi: ArtisanApiService,
    private clientApi: ClientApi,
    private categoryApi: CategoryApiService
  ) {}

  ngOnInit() {
    this.load();
    this.categoryApi.getAll(0, 200, 'name,asc').subscribe({
      next: (p) => (this.categories = p.content.map((c) => ({ id: c.id, name: c.name })) ),
      error: () => (this.categories = [])
    });
  }

  load(page = 0, size = 12) {
    this.loading = true;
    this.artisanApi
      .searchArtisans({
        keyword: this.keyword || undefined,
        city: this.city || undefined,
        categoryId: this.categoryId ?? undefined,
        page,
        size
      })
      .subscribe({
        next: (p: ArtisanPage<ArtisanDto>) => {
          this.total = p.totalElements;
          this.cards = p.content.map((a) => ({
            id: a.id,
            name: a.name,
            city: a.localisation ?? (a as any).city ?? '-',
            image: a.avatarUrl ?? (a as any).imageUrl ?? 'assets/Images/artisan.jpg',
            rating: a.rating ?? 0,
            metier: a.metier ?? a.categoryName ?? '',
            categoryId: (a as any).categoryId
          }));
        },
        error: (err) => console.error('searchArtisans failed', err),
        complete: () => (this.loading = false)
      });
  }

  onSearch() {
    this.load(0, 12);
  }

  // لعرض الطلب
  openRequestModal(card?: Card) {
    this.errorMsg = '';
    this.form = {
      title: card ? `${card.metier || card.name}` : '',
      city: card?.city || '',
      desiredDate: '',
      description: '',
      categoryId: card?.categoryId ?? this.categoryId ?? null
    };
    this.showModal = true;
  }
  closeModal() {
    if (!this.sending) this.showModal = false;
  }

  // تحويل تاريخ input ل ISO
  private toIsoDate(d?: string): string | undefined {
    if (!d) return undefined;
    const s = d.trim();
    if (!s) return undefined;
    if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s; // yyyy-MM-dd
    const m = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(s); // dd/MM/yyyy
    if (m) return `${m[3]}-${m[2]}-${m[1]}`;
    return undefined;
  }

  sendRequest() {
    this.errorMsg = '';
    if (!this.form.title.trim()) {
      this.errorMsg = 'Titre obligatoire.';
      return;
    }

    this.sending = true;

    const payload: ServiceRequestCreateRequest = {
      title: this.form.title.trim(),
      city: this.form.city.trim() || undefined,
      description: this.form.description.trim() || undefined,
      desiredDate: this.toIsoDate(this.form.desiredDate),
      categoryId: this.form.categoryId ?? undefined
    };

    this.clientApi.createRequest(payload).subscribe({
      next: () => {
        this.sending = false;
        this.showModal = false;
        alert('Demande envoyée ✔️');
      },
      error: (e) => {
        this.sending = false;
        const msg = e?.error?.message || e?.message || 'Unexpected error';
        this.errorMsg = `${msg} [${e?.status ?? ''}] · ${e?.url ?? ''}`;
      }
    });
  }

  // باش يصلّح الخطأ ديال trackBy
  trackById(_: number, c: Card) {
    return c.id;
  }
}
