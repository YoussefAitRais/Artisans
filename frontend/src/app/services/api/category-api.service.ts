import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Category {
  id: number;
  name: string;
  description?: string;
}

@Injectable({ providedIn: 'root' })
export class CategoryApi {
  private readonly API_BASE = 'http://localhost:8091';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.API_BASE}/api/categories`);
  }
}
