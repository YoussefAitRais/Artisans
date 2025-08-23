// src/app/pages/client/favorites/favorites.component.ts
import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FavoritesApi, FavoriteDto } from '../../../services/api/favorites-api.service';

interface Favorite {
  id: number;
  name: string;
  category: string;
  city: string;
  image: string;
  rating: number;
}

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './favorites.component.html'
})
export class FavoritesComponent implements OnInit {
  q = signal('');
  favorites = signal<Favorite[]>([]);

  constructor(private favApi: FavoritesApi) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.favApi.list().subscribe({
      next: (list) => this.favorites.set(list.map(this.toUi))
    });
  }

  private toUi = (d: FavoriteDto): Favorite => ({
    id: d.artisanId,
    name: d.artisanName,
    category: d.categoryName ?? '-',
    city: d.city ?? '-',
    image: d.imageUrl ?? 'assets/Images/artisan.jpg',
    rating: d.rating ?? 0
  });

  filtered() {
    const t = this.q().toLowerCase();
    return this.favorites().filter(f =>
      `${f.name} ${f.category} ${f.city}`.toLowerCase().includes(t)
    );
  }

  remove(f: Favorite) {
    if (!confirm(`Retirer ${f.name} des favoris ?`)) return;
    this.favApi.remove(f.id).subscribe({
      next: () => this.favorites.update(list => list.filter(x => x.id !== f.id))
    });
  }

  stars(n: number) { return Array.from({ length: 5 }, (_, i) => i < n); }
}
