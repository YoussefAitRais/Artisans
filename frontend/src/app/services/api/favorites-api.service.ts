// src/app/services/api/favorites-api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_BASE = 'http://localhost:8091';

export interface FavoriteDto {
  artisanId: number;
  artisanName: string;
  categoryName?: string;
  city?: string;
  imageUrl?: string;
  rating?: number;
}

@Injectable({ providedIn: 'root' })
export class FavoritesApi {
  private base = `${API_BASE}/api/client/favorites`;
  constructor(private http: HttpClient) {}

  list(): Observable<FavoriteDto[]> {
    return this.http.get<FavoriteDto[]>(this.base);
  }

  add(artisanId: number): Observable<void> {
    // Backend: POST /api/client/favorites/{artisanId}
    return this.http.post<void>(`${this.base}/${artisanId}`, {});
  }

  remove(artisanId: number): Observable<void> {
    // Backend: DELETE /api/client/favorites/{artisanId}
    return this.http.delete<void>(`${this.base}/${artisanId}`);
  }
}
