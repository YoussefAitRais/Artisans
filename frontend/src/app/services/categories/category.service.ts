import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Category {
  id: number;
  name: string;
  description?: string;
}
interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8091/api/categories';


  listAll(page = 0, size = 100): Observable<Category[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Category>>(this.base, { params }).pipe(
      map(p => p?.content ?? [])
    );
  }
}
