import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArtisanApiService, PortfolioItem } from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-portfolio',
  template: `
  <div class="p-4 space-y-4">
    <h2 class="text-xl font-bold">Portfolio</h2>

    <input type="file" (change)="onFile($event)" />

    <div class="grid gap-3 md:grid-cols-3">
      <div class="rounded border p-2" *ngFor="let it of items">
        <img [src]="it.imageUrl" alt="" class="w-full rounded mb-2">
        <div class="font-medium">{{ it.title }}</div>
        <div class="text-sm text-slate-500" *ngIf="it.description">{{ it.description }}</div>
      </div>
    </div>
  </div>
  `,
  imports: [CommonModule]
})
export class ArtisanPortfolioComponent implements OnInit {
  private api = inject(ArtisanApiService);

  items: PortfolioItem[] = [];

  ngOnInit(): void { this.reload(); }

  reload(): void {
    this.api.listPortfolio().subscribe((x: PortfolioItem[]) => (this.items = x));
  }

  onFile(e: Event) {
    const input = e.target as HTMLInputElement;
    const f = input.files?.[0];
    if (!f) return;
    this.api.uploadPortfolio(f).subscribe(() => this.reload());
  }
}
