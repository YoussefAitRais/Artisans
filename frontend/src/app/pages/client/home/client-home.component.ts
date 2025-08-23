import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ArtisanApiService,
  ArtisanDto,
  Page as ArtisanPage
} from '../../../services/api/artisan-api.service';

type Card = {
  id: number;
  name: string;
  city: string;
  image: string;
  rating: number;
  metier: string;
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

  // بحث بسيط
  keyword = '';
  city = '';
  categoryId: number | null = null;

  constructor(private artisanApi: ArtisanApiService) {}

  ngOnInit() {
    this.load();
  }

  load(page = 0, size = 12) {
    this.loading = true;
    this.artisanApi.searchArtisans({
      keyword: this.keyword || undefined,
      city: this.city || undefined,
      categoryId: this.categoryId ?? undefined,
      page, size
    }).subscribe({
      next: (p: ArtisanPage<ArtisanDto>) => {
        this.total = p.totalElements;
        this.cards = p.content.map(a => ({
          id: a.id,
          name: a.name,
          city: a.localisation ?? (a as any).city ?? '-',
          image: a.avatarUrl ?? (a as any).imageUrl ?? 'assets/Images/artisan.jpg',
          rating: a.rating ?? 0,
          metier: a.metier ?? a.categoryName ?? ''
        }));
      },
      error: (err) => console.error('searchArtisans failed', err),
      complete: () => this.loading = false
    });
  }

  onSearch() {
    this.load(0, 12);
  }
}
