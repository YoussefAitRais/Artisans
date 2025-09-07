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

export type ReqStatus =
  | 'PENDING'
  | 'RESPONDED'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'COMPLETED';

/* ===== Client ===== */
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

/* ===== Requests (devis) ===== */
export interface ServiceRequestResponse {
  id: number;
  categoryId?: number;
  title: string;
  city?: string;
  description?: string;
  desiredDate?: string; // yyyy-MM-dd
  status: ReqStatus;
  createdAt: string;    // ISO instant
  clientEmail?: string;
}

export interface ServiceRequestCreateRequest {
  categoryId?: number;
  title: string;
  city?: string;
  description?: string;
  desiredDate?: string; // yyyy-MM-dd
}

export interface ServiceRequestUpdateRequest extends ServiceRequestCreateRequest {}


@Injectable({ providedIn: 'root' })
export class ClientApi {
  constructor(private http: HttpClient) {}

  myRequests(page = 0, size = 10, sort = 'createdAt,desc') {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Page<ServiceRequestResponse>>(`${API_BASE}/api/requests`, { params });
  }

  createRequest(req: ServiceRequestCreateRequest) {
    return this.http.post<ServiceRequestResponse>(`${API_BASE}/api/requests`, req);
  }

  cancelRequest(id: number) {
    return this.http.post<void>(`${API_BASE}/api/requests/${id}/cancel`, {});
  }

  deleteRequest(id: number) {
    return this.http.delete<void>(`${API_BASE}/api/requests/${id}`);
  }

  getMe() { return this.http.get<ClientResponse>(`${API_BASE}/api/client/me`); }
  updateMe(req: ClientUpdateRequest) { return this.http.put<ClientResponse>(`${API_BASE}/api/client/me`, req); }
}
