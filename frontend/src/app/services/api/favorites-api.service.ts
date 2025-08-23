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
  constructor(private http: HttpClient) {}

  list(): Observable<FavoriteDto[]> {
    return this.http.get<FavoriteDto[]>(`${API_BASE}/api/client/favorites`);
  }

  add(artisanId: number) {
    return this.http.post<void>(`${API_BASE}/api/client/favorites/${artisanId}`, {});
  }

  remove(artisanId: number) {
    return this.http.delete<void>(`${API_BASE}/api/client/favorites/${artisanId}`);
  }
}
