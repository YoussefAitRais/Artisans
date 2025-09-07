import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ArtisanApiService,
  ArtisanDto,
  Page,
  Category,
} from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-client-home',
  imports: [CommonModule, FormsModule],
  templateUrl: './client-home.component.html',
})
export class ClientHomeComponent implements OnInit {
  private api = inject(ArtisanApiService);

  // Filters
  metier = '';
  city = '';
  keyword = '';
  categoryId: number | null = null;

  // View state
  loading = false;
  cards: Array<{ id: number; name: string; city: string; image: string; rating: number; metier: string }> = [];

  // Modal state for creating a request
  showModal = false;
  sending = false;
  errorMsg = '';
  categories: Category[] = [];
  form = {
    title: '',
    city: '',
    categoryId: null as number | null,
    desiredDate: '',
    description: '',
    artisanId: null as number | null,
  };

  ngOnInit(): void {
    this.search();
    this.api.listCategories().subscribe((x) => (this.categories = x));
  }

  onSearch(): void {
    this.search();
  }

  search(page = 0): void {
    this.loading = true;
    this.api
      .searchArtisans({
        metier: this.metier || undefined,
        localisation: this.city || undefined,
        q: this.keyword || undefined,
        page,
        size: 12,
      })
      .subscribe({
        next: (p: Page<ArtisanDto>) => {
          this.cards = p.content.map((a) => ({
            id: a.id,
            name: a.name,
            city: a.localisation ?? a.city ?? '-',
            image: a.avatarUrl ?? a.imageUrl ?? 'assets/img-placeholder.png',
            rating: a.rating ?? 0,
            metier: a.metier ?? a.categoryName ?? '',
          }));
        },
        complete: () => (this.loading = false),
      });
  }

  trackById(_: number, c: { id: number }): number {
    return c.id;
  }

  openRequestModal(card: { id: number; name: string }): void {
    // Prefill the form with the selected artisan
    this.form = {
      title: '',
      city: this.city || '',
      categoryId: this.categoryId,
      desiredDate: '',
      description: '',
      artisanId: card.id,
    };
    this.errorMsg = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  sendRequest(): void {
    if (!this.form.title || !this.form.city || !this.form.categoryId || !this.form.desiredDate) {
      this.errorMsg = 'Veuillez remplir les champs obligatoires.';
      return;
    }
    this.sending = true;
    this.api
      .createRequest({
        title: this.form.title,
        city: this.form.city,
        categoryId: this.form.categoryId,
        desiredDate: this.form.desiredDate,
        description: this.form.description,
        artisanId: this.form.artisanId ?? undefined,
      })
      .subscribe({
        next: () => {
          this.sending = false;
          this.showModal = false;
        },
        error: () => {
          this.errorMsg = 'Échec de l’envoi.';
          this.sending = false;
        },
      });
  }
}
