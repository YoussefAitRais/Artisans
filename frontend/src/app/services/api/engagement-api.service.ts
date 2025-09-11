import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// API base
const API_BASE = 'http://localhost:8091';

// Models and Types
export type EngagementStatus =
  | 'PENDING_CONFIRMATION'
  | 'SCHEDULED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export interface EngagementResponse {
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

export interface EngagementConfirmRequest {
  startDate: string; // yyyy-MM-dd
  endDate: string;   // yyyy-MM-dd
}

@Injectable({ providedIn: 'root' })
export class EngagementApiService {
  private base = `${API_BASE}/api/engagements`;

  constructor(private http: HttpClient) {}

  /** Get my engagements (role-aware) */
  myEngagements(page = 0, size = 10): Observable<Page<EngagementResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<EngagementResponse>>(`${this.base}/mine`, { params });
  }

  /** Get single engagement details */
  getEngagement(id: number): Observable<EngagementResponse> {
    return this.http.get<EngagementResponse>(`${this.base}/${id}`);
  }

  /** Artisan confirms schedule: PENDING_CONFIRMATION -> SCHEDULED */
  confirmAsArtisan(id: number, request: EngagementConfirmRequest): Observable<EngagementResponse> {
    return this.http.post<EngagementResponse>(`${this.base}/${id}/confirm`, request);
  }

  /*Start work: SCHEDULED -> IN_PROGRESS */
  startEngagement(id: number): Observable<EngagementResponse> {
    return this.http.post<EngagementResponse>(`${this.base}/${id}/start`, {});
  }

  /* Complete work: IN_PROGRESS -> COMPLETED (client action) */
  completeEngagement(id: number): Observable<EngagementResponse> {
    return this.http.post<EngagementResponse>(`${this.base}/${id}/complete`, {});
  }

  /* Cancel engagement: PENDING_CONFIRMATION/SCHEDULED -> CANCELLED */
  cancelEngagement(id: number): Observable<EngagementResponse> {
    return this.http.post<EngagementResponse>(`${this.base}/${id}/cancel`, {});
  }
}
