import { Component, OnInit, inject, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CategoryApiService, Category } from '../../../services/api/category-api.service';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './categories.component.html'
})
export class CategoriesComponent implements OnInit {
  private api = inject(CategoryApiService);
  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  loading = false;
  errorMsg = '';
  items: Category[] = [];
  q = '';

  /** خريطة صور مقترحة (بدّل الأسماء حسب الصور اللي عندك فـ assets) */
  private CATEGORY_IMG_MAP: Record<string, string> = {
    'plomberie':               'assets/categories/plomberie.jpg',
    'electricite':             'assets/categories/electricite.jpg',
    'menuiserie':              'assets/categories/menuiserie.jpg',
    'maconnerie':              'assets/categories/maconnerie.jpg',
    'peinture':                'assets/categories/peinture.jpg',
    'carrelage':               'assets/categories/carrelage.jpg',
    'serrurerie':              'assets/categories/serrurerie.jpg',
    'climatisation':           'assets/categories/climatisation.jpg',
    'chauffage':               'assets/categories/chauffage.jpg',
    'platrier-plaquiste':      'assets/categories/platrerie.jpg',
    'charpente-couverture':    'assets/categories/charpente.jpg',
    'aluminium-vitrerie':      'assets/categories/aluminium.jpg',
    'ferronnerie-metallerie':  'assets/categories/ferronnerie.jpg',
    'parquet-revetements':     'assets/categories/parquet.jpg',
    'paysagiste-jardinage':    'assets/categories/jardinage.jpg',
    'etancheite-isolation':    'assets/categories/etancheite.jpg',
    'nettoyage-fin-de-chantier':'assets/categories/nettoyage.jpg',
    'demenagement':            'assets/categories/demenagement.jpg',
  };

  ngOnInit() {
    // باش ما يديرش طلب SSR قبل ما يقلع الباك
    if (isPlatformBrowser(this.platformId)) this.load();
  }

  load() {
    this.loading = true;
    this.errorMsg = '';
    this.api.getAll(0, 100).subscribe({
      next: (p) => this.items = p?.content ?? [],
      error: (e) => {
        console.error('Categories load failed', e);
        this.errorMsg = 'Impossible de charger les catégories.';
        this.items = [];
      },
      complete: () => this.loading = false
    });
  }

  get filtered(): Category[] {
    const q = this.q.trim().toLowerCase();
    if (!q) return this.items;
    return this.items.filter(c =>
      c.name.toLowerCase().includes(q) ||
      (c.description || '').toLowerCase().includes(q)
    );
  }

  trackById(_: number, c: Category) { return c.id; }

  /** يحوّل الإسم لslug بسيط بلا تشكيل/مسافات */
  private slugify(s = '') {
    return s
      .normalize('NFD').replace(/[\u0300-\u036f]/g, '') // remove accents
      .toLowerCase()
      .replace(/&/g, 'and')
      .replace(/[^\w]+/g, '-')      // non word → -
      .replace(/(^-|-$)/g, '');
  }

  imageFor(c: Category): string {
    const key = this.slugify(c.name);
    return this.CATEGORY_IMG_MAP[key] || `assets/categories/${key}.jpg`;
    // أو رجّع fallback عام:
    // return this.CATEGORY_IMG_MAP[key] || 'assets/categories/default.jpg';
  }
}
