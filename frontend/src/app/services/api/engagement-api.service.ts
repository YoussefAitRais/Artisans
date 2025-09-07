import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// === API base ===
const API = 'http://localhost:8091/api/engagements';

// ====== Models ======
export type EngagementStatus =
  | 'PENDING_CONFIRMATION'
  | 'SCHEDULED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface Engagement {
  id: number;
  requestId: number;
  quoteId: number;
  clientId: number;
  artisanId: number;
  agreedPrice: number;
  startDate?: string; // yyyy-MM-dd
  endDate?: string;   // yyyy-MM-dd
  status: EngagementStatus;
  createdAt: string;  // ISO
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;      // current page (0-based)
  size: number;
}

export interface ConfirmDto {
  startDate: string; // yyyy-MM-dd
  endDate: string;   // yyyy-MM-dd
}

@Injectable({ providedIn: 'root' })
export class EngagementApiService {
  constructor(private http: HttpClient) {}

  /** engagements */
  getMine(page = 0, size = 10): Observable<Page<Engagement>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Engagement>>(`${API}/me`, { params });
  }

  confirm(id: number, dto: ConfirmDto): Observable<Engagement> {
    return this.http.patch<Engagement>(`${API}/${id}/confirm`, dto);
  }

  start(id: number): Observable<Engagement> {
    return this.http.patch<Engagement>(`${API}/${id}/start`, {});
  }

  cancel(id: number): Observable<Engagement> {
    return this.http.patch<Engagement>(`${API}/${id}/cancel`, {});
  }

  complete(id: number): Observable<Engagement> {
    return this.http.patch<Engagement>(`${API}/${id}/complete`, {});
  }
}
