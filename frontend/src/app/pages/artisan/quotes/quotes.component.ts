import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ArtisanApiService, Page, Quote } from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-quotes',
  imports: [CommonModule, FormsModule],
  templateUrl: './quotes.component.html'
})
export class ArtisanQuotesComponent implements OnInit {
  private api = inject(ArtisanApiService);

  loading = false;
  quotes: Quote[] = [];

  model = { requestId: null as number|null, price: null as number|null, estimatedDays: null as number|null, message: '' };

  ngOnInit(){ this.refresh(); }

  refresh(){
    this.loading = true;
    this.api.myQuotes().subscribe({
      next: (p: Page<Quote>) => this.quotes = p.content,
      complete: () => this.loading = false
    });
  }

  create(){
    if (!this.model.requestId || !this.model.price) return;
    this.api.createQuoteForRequest(this.model.requestId, {
      price: this.model.price,
      estimatedDays: this.model.estimatedDays ?? undefined,
      message: this.model.message || undefined
    }).subscribe(() => {
      this.model = { requestId: null, price: null, estimatedDays: null, message: '' };
      this.refresh();
    });
  }
}
