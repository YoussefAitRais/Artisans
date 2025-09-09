import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { NavbarComponent } from "../../../navbar/navbar.component";

type Category = { id: number; name: string; description?: string };

@Component({
  standalone: true,
  selector: 'app-categories',
  imports: [CommonModule, NavbarComponent],
  templateUrl: './categories.component.html'
})
export class CategoriesComponent implements OnInit {
  private http = inject(HttpClient);

  items: Category[] = [];
  loading = false;
  error = '';
  success = '';

  // Category images mapping for better visual representation
  private categoryImages: { [key: string]: string } = {
    'Plomberie': 'assets/Images/plomberie.jpg',
    'Électricité': 'assets/Images/electricite.jpg',
    'Peinture': 'assets/Images/peinture.jpg',
    'Menuiserie': 'assets/Images/menuiserie.jpg',
    'Jardinage': 'assets/Images/jardinage.jpg',
    'Nettoyage': 'assets/Images/nettoyage.jpg',
    'Réparation': 'assets/Images/reparation.jpg',
    'Construction': 'assets/Images/construction.jpg',
    'Climatisation': 'assets/Images/climatisation.jpg',
    'Carrelage': 'assets/Images/carrelage.jpg'
  };

  // Category icons mapping
  private categoryIcons: { [key: string]: string } = {
    'Plomberie': '🔧',
    'Électricité': '⚡',
    'Peinture': '🎨',
    'Menuiserie': '🪚',
    'Jardinage': '🌱',
    'Nettoyage': '🧽',
    'Réparation': '🔨',
    'Construction': '🏗️',
    'Climatisation': '❄️',
    'Carrelage': '🧱',
    'default': '🛠️'
  };

  ngOnInit(): void {
    this.loadCategories();
  }

  /**
   * Load categories from the API
   */
  loadCategories(): void {
    this.loading = true;
    this.error = '';
    
    const params = new HttpParams().set('page', 0).set('size', 100);
    
    this.http.get<{content: Category[]}>('http://localhost:8091/api/categories', { params })
      .subscribe({
        next: (response) => {
          this.items = response?.content ?? [];
          if (this.items.length > 0) {
            this.success = `Found ${this.items.length} categories`;
            setTimeout(() => this.success = '', 3000); // Clear success message after 3 seconds
          }
        },
        error: (err) => {
          console.error('Error loading categories:', err);
          this.error = 'Failed to load categories. Please try again.';
        },
        complete: () => {
          this.loading = false;
        }
      });
  }

  /**
   * Get category image with fallback
   */
  getCategoryImage(categoryName: string): string {
    const image = this.categoryImages[categoryName];
    if (image) {
      return image;
    }
    
    // Fallback to a beautiful gradient placeholder
    const colors = [
      'from-blue-500 to-purple-600',
      'from-green-500 to-teal-600', 
      'from-pink-500 to-rose-600',
      'from-yellow-500 to-orange-600',
      'from-indigo-500 to-blue-600',
      'from-purple-500 to-pink-600'
    ];
    
    const colorIndex = categoryName.length % colors.length;
    const gradientClass = colors[colorIndex];
    
    // Return a data URL for a simple gradient background
    return `data:image/svg+xml;base64,${btoa(`
      <svg width="400" height="200" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:rgb(99, 102, 241);stop-opacity:1" />
            <stop offset="100%" style="stop-color:rgb(168, 85, 247);stop-opacity:1" />
          </linearGradient>
        </defs>
        <rect width="400" height="200" fill="url(#grad)" />
        <text x="200" y="100" font-family="Arial, sans-serif" font-size="24" font-weight="bold" text-anchor="middle" dominant-baseline="middle" fill="white">${categoryName}</text>
      </svg>
    `)}`;
  }

  /**
   * Get category icon with fallback
   */
  getCategoryIcon(categoryName: string): string {
    return this.categoryIcons[categoryName] || this.categoryIcons['default'];
  }

  /**
   * Generate random artisan count for display
   */
  getRandomArtisanCount(): number {
    return Math.floor(Math.random() * 50) + 10; // Random number between 10-59
  }
}
