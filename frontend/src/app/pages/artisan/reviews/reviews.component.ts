import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-artisan-reviews',
  imports: [CommonModule],
  templateUrl: './reviews.component.html'
})
export class ArtisanReviewsComponent {
  reviews = [
    { author: 'Amine',   rating: 5, text: 'Excellent travail.' },
    { author: 'Salma',   rating: 4, text: 'Très bien, ponctuel.' },
    { author: 'Yassin',  rating: 3, text: 'Correct, à améliorer.' },
  ];
}
