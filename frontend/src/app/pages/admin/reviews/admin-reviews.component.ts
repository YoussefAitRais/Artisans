import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type ReviewStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface Review {
  id: number;
  user: string;
  artisan: string;
  rating: number; // 1..5
  comment: string;
  status: ReviewStatus;
  createdAt: string;
}

@Component({
  selector: 'app-admin-reviews',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reviews.component.html'
})
export class AdminReviewsComponent {
  reviews = signal<Review[]>([
    { id: 1, user: 'Sara', artisan: 'Plombier Pro', rating: 5, comment: 'Excellent service!', status: 'APPROVED', createdAt: '2024-07-10' },
    { id: 2, user: 'Omar', artisan: 'Électricien+ ', rating: 3, comment: 'Correct', status: 'PENDING', createdAt: '2024-08-02' },
    { id: 3, user: 'Salma', artisan: 'PeintureFix', rating: 2, comment: 'En retard', status: 'REJECTED', createdAt: '2024-08-05' },
    { id: 4, user: 'Nadia', artisan: 'ClimTech', rating: 4, comment: 'Très bien', status: 'APPROVED', createdAt: '2024-08-07' },
  ]);

  q = signal('');
  status = signal<ReviewStatus | 'ALL'>('ALL');
  minRating = signal<number | 'ALL'>('ALL');

  filtered = computed(() => {
    const text = this.q().toLowerCase();
    const st = this.status();
    const min = this.minRating();
    return this.reviews().filter(r => {
      const t = [r.user, r.artisan, r.comment, r.status].join(' ').toLowerCase().includes(text);
      const s = st === 'ALL' || r.status === st;
      const m = min === 'ALL' || r.rating >= min;
      return t && s && m;
    });
  });

  approve(r: Review) { this.reviews.update(list => list.map(x => x.id === r.id ? { ...x, status: 'APPROVED' } : x)); }
  reject(r: Review)  { this.reviews.update(list => list.map(x => x.id === r.id ? { ...x, status: 'REJECTED' } : x)); }
  remove(r: Review)  { if (confirm('Supprimer cet avis ?')) this.reviews.update(l => l.filter(x => x.id !== r.id)); }

  // helpers
  stars(n: number) { return Array.from({ length: 5 }, (_, i) => i < n); }
  trackById = (_: number, r: Review) => r.id;
}
