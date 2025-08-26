import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

/* ======= أنواع عامة ======= */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages?: number;
  size?: number;
  number?: number; // zero-based
}

export interface Category {
  id: number;
  name: string;
  description?: string;
}

/* ======= Public search DTO ======= */
export interface ArtisanDto {
  id: number;
  name: string;
  metier?: string;
  localisation?: string;
  rating?: number;
  avatarUrl?: string;
  categoryName?: string;
  // توافق مع كود قديم
  city?: string;
  imageUrl?: string;
}

/* ======= Profile ======= */
export interface ArtisanProfile {
  id?: number;
  metier?: string;
  localisation?: string;
  description?: string;
  category?: Category | { id: number };
}

/* ======= Requests ======= */
export type RequestStatus =
  | 'NOUVELLE' | 'EN_ATTENTE' | 'REPONDUE' | 'REFUSEE' | 'ANNULEE';

export interface ArtisanRequest {
  id: number;
  titre: string;
  ville?: string;
  description?: string;
  createdAt?: string;
  status?: RequestStatus;
  clientName?: string;
}

/* ======= Quotes (Devis) ======= */
export type QuoteStatus = 'BROUILLON' | 'ENVOYE' | 'ACCEPTE' | 'REFUSE';

export interface Quote {
  id: number;
  ref?: string;
  montant: number;
  message?: string;
  dateProposition?: string;
  demandeId: number;
  status?: QuoteStatus;
}

/* ======= Reviews ======= */
export interface Review {
  id: number;
  author: string;
  rating: number;
  text?: string;
  createdAt?: string;
}

/* ======= Availability ======= */
export interface AvailabilitySlot {
  id: number;
  dayOfWeek: number; // 1..7
  startTime: string; // "09:00"
  endTime: string;   // "13:00"
}

/* ======= Portfolio ======= */
export interface PortfolioItem {
  id: number;
  url: string;
  title?: string;
  createdAt?: string;
}

/* ======= Inbox / Offers ======= */
export interface ArtisanInboxItem {
  id: number;
  title: string;
  city?: string;
  description?: string;
  desiredDate?: string; // yyyy-MM-dd
  createdAt: string;    // ISO
  status: 'PENDING' | 'RESPONDED' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';
  categoryId?: number;
  clientEmail?: string;
}

export interface OfferCreateRequest {
  price: number;
  message?: string;
  estimatedDays?: number;
}

export interface OfferResponse {
  id: number;
  requestId: number;
  price: number;
  message?: string;
  estimatedDays?: number;
  createdAt: string;
  status: 'SENT' | 'WITHDRAWN' | 'ACCEPTED' | 'REJECTED';
}

/* ======= Service ======= */

@Injectable({ providedIn: 'root' })
export class ArtisanApiService {
  private http = inject(HttpClient);
  private readonly API = 'http://localhost:8091/api';

  /* --- Public search (client side) --- */
  searchArtisans(opts?: {
    keyword?: string; categoryId?: number; city?: string; page?: number; size?: number;
  }): Observable<Page<ArtisanDto>> {
    let params = new HttpParams();
    if (opts?.keyword)      params = params.set('q', opts.keyword);
    if (opts?.categoryId)   params = params.set('categoryId', String(opts.categoryId));
    if (opts?.city)         params = params.set('city', opts.city);
    if (opts?.page != null) params = params.set('page', String(opts.page));
    if (opts?.size != null) params = params.set('size', String(opts.size));
    return this.http.get<Page<ArtisanDto>>(`${this.API}/artisans`, { params });
  }

  /* --- Profile --- */
  getMyProfile(): Observable<ArtisanProfile> {
    return this.http.get<ArtisanProfile>(`${this.API}/artisan/me`);
  }
  updateMyProfile(dto: ArtisanProfile & { categoryId?: number }): Observable<ArtisanProfile> {
    return this.http.put<ArtisanProfile>(`${this.API}/artisan/me`, dto);
  }

  /* --- Requests list for artisan --- */
  getRequests(opts?: { status?: RequestStatus; page?: number; size?: number })
    : Observable<Page<ArtisanRequest>> {
    let params = new HttpParams();
    if (opts?.status)       params = params.set('status', opts.status);
    if (opts?.page != null) params = params.set('page', String(opts.page));
    if (opts?.size != null) params = params.set('size', String(opts.size));
    return this.http.get<Page<ArtisanRequest>>(`${this.API}/artisan/requests`, { params });
  }

  /* --- Quotes --- */
  listQuotes(): Observable<Page<Quote>> {
    return this.http.get<Page<Quote>>(`${this.API}/artisan/quotes`);
  }
  createQuote(body: { demandeId: number; montant: number; message?: string }): Observable<Quote> {
    return this.http.post<Quote>(`${this.API}/artisan/quotes`, body);
  }
  updateQuote(id: number, body: Partial<Quote>): Observable<Quote> {
    return this.http.put<Quote>(`${this.API}/artisan/quotes/${id}`, body);
  }
  setQuoteStatus(id: number, status: QuoteStatus): Observable<Quote> {
    return this.http.patch<Quote>(`${this.API}/artisan/quotes/${id}/status`, { status });
  }

  /* --- Availability --- */
  listAvailability(): Observable<AvailabilitySlot[]> {
    return this.http.get<AvailabilitySlot[]>(`${this.API}/artisan/availability`);
  }
  addAvailability(slot: Omit<AvailabilitySlot, 'id'>): Observable<AvailabilitySlot> {
    return this.http.post<AvailabilitySlot>(`${this.API}/artisan/availability`, slot);
  }
  deleteAvailability(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/artisan/availability/${id}`);
  }

  /* --- Portfolio --- */
  listPortfolio(): Observable<PortfolioItem[]> {
    return this.http.get<PortfolioItem[]>(`${this.API}/artisan/portfolio`);
  }
  uploadPortfolio(file: File, title?: string): Observable<PortfolioItem> {
    const fd = new FormData();
    fd.append('file', file);
    if (title) fd.append('title', title);
    return this.http.post<PortfolioItem>(`${this.API}/artisan/portfolio`, fd);
  }
  deletePortfolio(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/artisan/portfolio/${id}`);
  }

  /* --- Reviews --- */
  listReviews(): Observable<Page<Review>> {
    return this.http.get<Page<Review>>(`${this.API}/artisan/reviews`);
  }

  /* --- Inbox & Offers --- */
  inbox(
    page = 0,
    size = 10,
    status: 'PENDING' | 'RESPONDED' | 'ALL' = 'PENDING'
  ): Observable<Page<ArtisanInboxItem>> {
    let params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('status', status);
    return this.http.get<Page<ArtisanInboxItem>>(`${this.API}/artisan/inbox`, { params });
  }

  sendOffer(requestId: number, body: OfferCreateRequest): Observable<OfferResponse> {
    return this.http.post<OfferResponse>(
      `${this.API}/artisan/requests/${requestId}/offers`,
      body
    );
  }
}
