import { Component, AfterViewInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-cards',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cards.component.html',
  styleUrls: ['./cards.component.css']
})
export class CardsComponent implements AfterViewInit {

  services = [
    { name: 'Forgeron', image: 'assets/Images/artisan.jpg' },
    { name: 'Électricien', image: 'assets/Images/artisan.jpg' },
    { name: 'Ouvrier du bâtiment', image: 'assets/Images/artisan.jpg' },
    { name: 'Plombier', image: 'assets/Images/artisan.jpg' },
    { name: 'Femme de ménage', image: 'assets/Images/artisan.jpg' },
    { name: 'Technicien climatisation', image: 'assets/Images/artisan.jpg' },
    { name: 'Peintre', image: 'assets/Images/artisan.jpg' },
    { name: 'Mécanicien', image: 'assets/Images/artisan.jpg' },
  ];

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  ngAfterViewInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    queueMicrotask(() => {
      (globalThis as any)?.HSStaticMethods?.autoInit?.();
    });
  }
}
