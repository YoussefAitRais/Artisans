import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';

type Category = { id: number; name: string; description?: string };

@Component({
  standalone: true,
  selector: 'app-categories',
  imports: [CommonModule],
  templateUrl: './categories.component.html'
})
export class CategoriesComponent implements OnInit {
  private http = inject(HttpClient);

  items: Category[] = [];
  loading = false;
  error = '';
  success = '';

  ngOnInit(): void {
    this.loading = true;
    const params = new HttpParams().set('page', 0).set('size', 100);
    this.http.get<{content: Category[]}>('http://localhost:8091/api/categories', { params })
      .subscribe({
        next: (p) => this.items = p?.content ?? [],
        error: () => this.error = 'تعذر جلب التصنيفات',
        complete: () => this.loading = false
      });
  }
}
