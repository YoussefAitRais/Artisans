import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArtisanApiService, Review } from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-home',
  imports: [CommonModule],
  templateUrl: './artisan-home.component.html'
})
export class ArtisanHomeComponent implements OnInit {
  private api = inject(ArtisanApiService);

  stats = [
    { key: 'pending', label: 'Demandes en attente', value: 0 },
    { key: 'sentQuotes', label: 'Devis envoyés', value: 0 },
    { key: 'reviews', label: 'Avis reçus', value: 0 },
  ];

  recentReviews: Review[] = [];

  ngOnInit() {
    this.api.getRequests({ status: 'EN_ATTENTE', page: 0, size: 1 })
      .subscribe(p => this.stats.find(s => s.key==='pending')!.value = p.totalElements);

    this.api.listQuotes().subscribe(p =>
      this.stats.find(s => s.key==='sentQuotes')!.value =
        p.content.filter(q => q.status === 'ENVOYE' || q.status === 'ACCEPTE').length
    );

    this.api.listReviews().subscribe(p => {
      this.stats.find(s => s.key==='reviews')!.value = p.totalElements;
      this.recentReviews = p.content.slice(0, 3);
    });
  }
}
