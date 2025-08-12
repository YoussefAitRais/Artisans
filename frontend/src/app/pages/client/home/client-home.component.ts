import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface RecentRequest {
  id: number;
  title: string;
  status: 'En attente' | 'Acceptée' | 'Refusée' | 'Terminée';
  date: string; // ISO
}

interface ArtisanCard {
  id: number;
  name: string;
  category: string;
  city: string;
  image: string;
  rating: number; // 1..5
}

@Component({
  selector: 'app-client-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './client-home.component.html'
})
export class ClientHomeComponent {
  userName = 'Youssef';

  // إحصائيات سريعة (mock)
  stats = {
    requests: 4,
    pending: 2,
    approved: 1,
    favorites: 3
  };

  // آخر الطلبات
  recent = signal<RecentRequest[]>([
    { id: 101, title: 'Réparation clim', status: 'En attente', date: '2025-08-10' },
    { id: 102, title: 'Plomberie cuisine', status: 'Acceptée',  date: '2025-08-07' },
    { id: 103, title: 'Peinture salon',  status: 'En attente', date: '2025-08-01' }
  ]);

  // حرفيين مقترحين
  recommended = signal<ArtisanCard[]>([
    { id: 1, name: 'ClimTech',       category: 'Climatisation', city: 'Casablanca', image: 'assets/Images/artisan.jpg', rating: 5 },
    { id: 2, name: 'Plombier Pro',   category: 'Plomberie',     city: 'Rabat',      image: 'assets/Images/artisan.jpg', rating: 4 },
    { id: 3, name: 'PeintureFix',    category: 'Peinture',      city: 'Fès',        image: 'assets/Images/artisan.jpg', rating: 4 },
    { id: 4, name: 'ElectroMaster',  category: 'Électricité',   city: 'Marrakech',  image: 'assets/Images/artisan.jpg', rating: 5 }
  ]);

  stars(n: number) { return Array.from({ length: 5 }, (_, i) => i < n); }
}
