import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-artisan-portfolio',
  imports: [CommonModule],
  templateUrl: './portfolio.component.html'
})
export class ArtisanPortfolioComponent {
  images = [
    'https://images.unsplash.com/photo-1581092337639-9d5118e1eab3?q=80&w=600',
    'https://images.unsplash.com/photo-1581093458791-9a40f6f3f4b2?q=80&w=600',
    'https://images.unsplash.com/photo-1505692952044-b77d75c2b3d1?q=80&w=600'
  ];
}
