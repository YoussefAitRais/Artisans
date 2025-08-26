import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArtisanApiService, PortfolioItem } from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-portfolio',
  imports: [CommonModule],
  templateUrl: './portfolio.component.html'
})
export class ArtisanPortfolioComponent implements OnInit {
  private api = inject(ArtisanApiService);
  items: PortfolioItem[] = [];

  ngOnInit() { this.reload(); }
  reload() { this.api.listPortfolio().subscribe(x => this.items = x); }

  upload(ev: Event) {
    const f = (ev.target as HTMLInputElement).files?.[0];
    if (!f) return;
    this.api.uploadPortfolio(f).subscribe(() => this.reload());
  }
}
