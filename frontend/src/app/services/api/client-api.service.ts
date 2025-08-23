import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_BASE = 'http://localhost:8091';

// ====== Types ======
export type ReqStatus = 'PENDING' | 'RESPONDED' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // zero-based
}

export interface CategoryDto {
  id: number;
  name: string;
  description?: string;
}

export interface ClientResponse {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
}

export interface ClientUpdateRequest {
  nom: string;
  prenom?: string;
  telephone?: string;
}

export interface ServiceRequestResponse {
  id: number;
  categoryId?: number;
  title: string;
  city?: string;
  description?: string;
  desiredDate?: string; // ISO (yyyy-MM-dd)
  status: ReqStatus;
  createdAt: string; // Instant
  clientEmail?: string;
}

export interface ServiceRequestCreateRequest {
  categoryId?: number;
  title: string;
  city?: string;
  description?: string;
  desiredDate?: string; // ISO
}

export interface ServiceRequestUpdateRequest extends ServiceRequestCreateRequest {}

// ====== Services ======
@Injectable({ providedIn: 'root' })
export class CategoryApi {
  private base = `${API_BASE}/api/categories`;
  constructor(private http: HttpClient) {}

  // نجيبو جميع الكاتيجوريز (pageable من السيرفر)
  getAll(page = 0, size = 50, sort = 'name,asc') {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Page<CategoryDto>>(this.base, { params });
  }
}

@Injectable({ providedIn: 'root' })
export class ClientApi {
  constructor(private http: HttpClient) {}

  // ------- Profile -------
  getMe(): Observable<ClientResponse> {
    return this.http.get<ClientResponse>(`${API_BASE}/api/client/me`);
  }

  updateMe(req: ClientUpdateRequest): Observable<ClientResponse> {
    return this.http.put<ClientResponse>(`${API_BASE}/api/client/me`, req);
  }

  // ------- Requests (Devis) -------
  myRequests(page = 0, size = 10, sort = 'createdAt,desc'): Observable<Page<ServiceRequestResponse>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Page<ServiceRequestResponse>>(`${API_BASE}/api/requests`, { params });
  }

  createRequest(req: ServiceRequestCreateRequest): Observable<ServiceRequestResponse> {
    return this.http.post<ServiceRequestResponse>(`${API_BASE}/api/requests`, req);
  }

  updateRequest(id: number, req: ServiceRequestUpdateRequest): Observable<ServiceRequestResponse> {
    return this.http.put<ServiceRequestResponse>(`${API_BASE}/api/requests/${id}`, req);
  }

  cancelRequest(id: number): Observable<void> {
    return this.http.post<void>(`${API_BASE}/api/requests/${id}/cancel`, {});
  }

  deleteRequest(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/api/requests/${id}`);
  }
}
