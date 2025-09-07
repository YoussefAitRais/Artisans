import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Page, Quote, ArtisanApiService, Review, ArtisanInboxItem } from '../../../services/api/artisan-api.service';

type Stat = { key: string; label: string; value: number };

@Component({
  selector: 'app-artisan-home',
  templateUrl: './artisan-home.component.html',
  standalone: true,
  imports: [CommonModule, RouterModule],
})
export class ArtisanHomeComponent implements OnInit {
  stats: Stat[] = [
    { key: 'inboxResponded', label: 'Demandes répondues', value: 0 },
    { key: 'sentQuotes',     label: 'Devis envoyés',       value: 0 },
    { key: 'avgRating',      label: 'Note moyenne',        value: 0 },
  ];

  recentReviews: Review[] = [];

  constructor(private readonly api: ArtisanApiService) {}

  private setStat(key: string, value: number) {
    const s = this.stats.find((x) => x.key === key);
    if (s) s.value = value;
  }

  ngOnInit(): void {
    this.api.inbox(0, 50, 'ALL').subscribe((p: Page<ArtisanInboxItem>) => {
      const responded = p.content.filter((i) => i.status === 'RESPONDED').length;
      this.setStat('inboxResponded', responded);
    });

    this.api.listQuotes().subscribe((p: Page<Quote>) => {
      this.setStat('sentQuotes', p.totalElements ?? p.content.length);
    });

    this.api.listReviews().subscribe((p: Page<Review>) => {
      this.recentReviews = p.content.slice(0, 3);
      const ratings = p.content.map((r) => r.rating ?? 0);
      const avg = ratings.length ? Math.round((ratings.reduce((a, b) => a + b, 0) / ratings.length) * 10) / 10 : 0;
      this.setStat('avgRating', avg);
    });
  }
}
