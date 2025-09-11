import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

//Generic pagination wrapper (Spring Data Page)
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // current page (0-based)
  size: number;
}

export type InboxStatus = 'ALL' | 'PENDING' | 'RESPONDED';

export interface ArtisanInboxItem {
  id: number;
  title: string;
  city?: string;
  description?: string;
  desiredDate?: string; // yyyy-MM-dd
  createdAt: string;    // ISO instant
  status: 'PENDING' | 'RESPONDED';
  categoryId?: number | null;
  clientEmail?: string | null;
}

export interface Quote {
  id: number;
  requestId: number;
  artisanId: number;
  price: number;
  message?: string | null;
  status: 'SENT' | 'ACCEPTED' | 'REJECTED';
  createdAt: string;
  estimatedDays?: number | null;
}

export interface Review {
  id: number;
  author: string;
  rating: number;
  text: string;
  createdAt?: string;
}

export interface AvailabilitySlot {
  id?: number;
  dayOfWeek: number;   // 1..7
  startTime: string;   // "09:00"
  endTime: string;     // "17:00"
}

export interface ArtisanProfile {
  metier: string;
  localisation: string;
  description: string;
}

export interface PortfolioItem {
  id: number;
  title: string;
  imageUrl: string;
  description?: string;
  createdAt?: string;
}

export interface ArtisanDto {
  id: number;
  name: string;
  localisation?: string;
  avatarUrl?: string;
  categoryName?: string;
  metier?: string;
  rating?: number;
  imageUrl?: string;
  city?: string;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
}

export interface SearchArtisanParams {
  metier?: string;
  localisation?: string;
  q?: string;
  page?: number;
  size?: number;
}

export interface ClientRequestCreate {
  title: string;
  city: string;
  description: string;
  desiredDate: string; // yyyy-MM-dd
  categoryId: number;
  artisanId?: number;
}

@Injectable({ providedIn: 'root' })
export class ArtisanApiService {
  private readonly base = 'http://localhost:8091/api';

  constructor(private http: HttpClient) {}

  /** Inbox (artisan) — GET /api/artisan/requests */
  inbox(page = 0, size = 10, status: InboxStatus = 'ALL', q?: string): Observable<Page<ArtisanInboxItem>> {
    let params = new HttpParams().set('page', page).set('size', size).set('status', status);
    if (q) params = params.set('q', q);
    return this.http.get<Page<ArtisanInboxItem>>(`${this.base}/artisan/requests`, { params });
  }

  /** Quotes (artisan) — GET /api/artisan/quotes */
  myQuotes(page = 0, size = 20): Observable<Page<Quote>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Quote>>(`${this.base}/artisan/quotes`, { params });
  }

  listQuotes(page = 0, size = 20): Observable<Page<Quote>> { return this.myQuotes(page, size); }

  /** Create quote — POST /api/requests/{requestId}/quotes */
  createQuoteForRequest(requestId: number, body: { price: number; estimatedDays?: number | null; message?: string | null }): Observable<Quote> {
    const payload = { price: body.price, estimatedDays: body.estimatedDays ?? null, message: body.message ?? null };
    return this.http.post<Quote>(`${this.base}/requests/${requestId}/quotes`, payload);
  }

  listAvailability(): Observable<AvailabilitySlot[]> {
    return this.http.get<AvailabilitySlot[]>(`${this.base}/artisan/availability`);
  }
  addAvailability(slot: AvailabilitySlot): Observable<AvailabilitySlot> {
    return this.http.post<AvailabilitySlot>(`${this.base}/artisan/availability`, slot);
  }
  deleteAvailability(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/artisan/availability/${id}`);
  }

  /** Portfolio */
  listPortfolio(): Observable<PortfolioItem[]> {
    return this.http.get<PortfolioItem[]>(`${this.base}/artisan/portfolio`);
  }
  uploadPortfolio(file: File): Observable<PortfolioItem> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<PortfolioItem>(`${this.base}/artisan/portfolio`, fd);
  }

  /** Reviews (artisan) */
  listReviews(page = 0, size = 20): Observable<Page<Review>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Review>>(`${this.base}/reviews/me`, { params });
  }

  /** Profile */
  getMyProfile(): Observable<ArtisanProfile> { return this.http.get<ArtisanProfile>(`${this.base}/artisan/profile`); }
  updateMyProfile(body: ArtisanProfile): Observable<ArtisanProfile> { return this.http.put<ArtisanProfile>(`${this.base}/artisan/profile`, body); }

  listCategories(): Observable<Category[]> {
    const params = new HttpParams().set('page', 0).set('size', 1000);
    return this.http.get<Page<Category>>(`${this.base}/categories`, { params }).pipe(map(p => p.content));
  }

  searchArtisans(params: SearchArtisanParams): Observable<Page<ArtisanDto>> {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && String(v) !== '') httpParams = httpParams.set(k, String(v));
    });
    return this.http.get<Page<ArtisanDto>>(`${this.base}/artisans`, { params: httpParams });
  }

  createRequest(body: ClientRequestCreate): Observable<{ id: number }> {
    return this.http.post<{ id: number }>(`${this.base}/requests`, body);
  }
}
