import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_BASE = 'http://localhost:8091';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // zero-based
}

export interface Category {
  id: number;
  name: string;
  description?: string;
}

@Injectable({ providedIn: 'root' })
export class CategoryApiService {
  private base = `${API_BASE}/api/categories`;
  constructor(private http: HttpClient) {}

  getAll(page?: number, size?: number, sort?: string): Observable<Page<Category>> {
    let params = new HttpParams();
    if (page != null) params = params.set('page', page);
    if (size != null) params = params.set('size', size);
    if (sort) params = params.set('sort', sort);
    return this.http.get<Page<Category>>(this.base, { params });
  }

  getById(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.base}/${id}`);
  }
}
