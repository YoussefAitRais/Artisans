import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

interface Favorite {
  id: number;
  name: string;
  category: string;
  city: string;
  image: string;
  rating: number; // 1..5
}

@Component({
  selector: 'app-favorites',
  standalone: true,
  imports: [CommonModule , FormsModule],
  templateUrl: './favorites.component.html'
})
export class FavoritesComponent {
  q = signal('');
  favorites = signal<Favorite[]>([
    { id: 1, name: 'ClimTech',      category: 'Climatisation', city: 'Casablanca', image: 'assets/Images/artisan.jpg', rating: 5 },
    { id: 2, name: 'Plombier Pro',  category: 'Plomberie',     city: 'Rabat',      image: 'assets/Images/artisan.jpg', rating: 4 },
    { id: 3, name: 'PeintureFix',   category: 'Peinture',      city: 'Fès',        image: 'assets/Images/artisan.jpg', rating: 4 },
  ]);

  filtered() {
    const t = this.q().toLowerCase();
    return this.favorites().filter(f =>
      `${f.name} ${f.category} ${f.city}`.toLowerCase().includes(t)
    );
  }

  remove(f: Favorite) {
    this.favorites.update(list => list.filter(x => x.id !== f.id));
  }

  stars(n: number) { return Array.from({ length: 5 }, (_, i) => i < n); }
}
