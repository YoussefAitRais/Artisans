import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ArtisanApiService, Quote } from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-quotes',
  imports: [CommonModule, FormsModule],
  templateUrl: './quotes.component.html'
})
export class ArtisanQuotesComponent implements OnInit {
  private api = inject(ArtisanApiService);

  quotes: Quote[] = [];
  loading = false;

  // form بسيط لإرسال devis جديد
  model = { demandeId: null as number | null, montant: null as number | null, message: '' };

  ngOnInit() { this.refresh(); }

  refresh() {
    this.loading = true;
    this.api.listQuotes().subscribe({
      next: (p) => this.quotes = p.content,
      error: () => {},
      complete: () => this.loading = false
    });
  }

  create() {
    if (!this.model.demandeId || !this.model.montant) return;
    this.api.createQuote({ demandeId: this.model.demandeId, montant: this.model.montant, message: this.model.message || undefined })
      .subscribe(() => { this.model = { demandeId: null, montant: null, message: '' }; this.refresh(); });
  }
}
