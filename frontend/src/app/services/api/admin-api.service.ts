import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { forkJoin, map, Observable } from 'rxjs';

// نفس الـ API base ديال باقي الخدمات
const API_BASE = 'http://localhost:8091';

// صفحة عامة
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // 0-based
  size: number;
}

// ===== Categories DTOs =====
export interface CategoryResponse {
  id: number;
  name: string;
  description?: string;
}
export interface CategoryCreateRequest {
  name: string;
  description?: string;
}
export interface CategoryUpdateRequest {
  name: string;
  description?: string;
}

// ===== Clients DTOs (مطابقة للبّاك لي وريتيني) =====
export interface ClientResponse {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
}

// ===== Requests =====
export type RequestStatus =
  | 'PENDING' | 'RESPONDED' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';

export interface ServiceRequestResponse {
  id: number;
  categoryId?: number | null;
  title: string;
  city?: string;
  description?: string;
  desiredDate?: string; // ISO (LocalDate), خليه string
  status: RequestStatus;
  createdAt: string;    // ISO
  clientEmail?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminApi {
  constructor(private http: HttpClient) {}

  // --------- Categories ----------
  categories(page = 0, size = 10, sort = 'id,desc'): Observable<Page<CategoryResponse>> {
    const params = new HttpParams({ fromObject: { page, size, sort } as any });
    return this.http.get<Page<CategoryResponse>>(`${API_BASE}/api/categories`, { params });
  }

  createCategory(body: CategoryCreateRequest): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(`${API_BASE}/api/admin/categories`, body);
  }

  updateCategory(id: number, body: CategoryUpdateRequest): Observable<CategoryResponse> {
    return this.http.put<CategoryResponse>(`${API_BASE}/api/admin/categories/${id}`, body);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/api/admin/categories/${id}`);
  }

  // --------- Clients (admin) ----------
  clients(q = '', page = 0, size = 10, sort = 'id,desc'): Observable<Page<ClientResponse>> {
    const params = new HttpParams({
      fromObject: {
        ...(q ? { q } : {}),
        page,
        size,
        sort
      } as any
    });
    return this.http.get<Page<ClientResponse>>(`${API_BASE}/api/admin/clients`, { params });
  }

  // --------- Requests (admin) ----------
  requests(opts: {
    categoryId?: number;
    status?: RequestStatus;
    city?: string;
    page?: number;
    size?: number;
    sort?: string;
  }): Observable<Page<ServiceRequestResponse>> {
    const params = new HttpParams({
      fromObject: {
        ...(opts.categoryId ? { categoryId: opts.categoryId } : {}),
        ...(opts.status ? { status: opts.status } : {}),
        ...(opts.city ? { city: opts.city } : {}),
        page: String(opts.page ?? 0),
        size: String(opts.size ?? 10),
        sort: opts.sort ?? 'id,desc'
      }
    });
    return this.http.get<Page<ServiceRequestResponse>>(`${API_BASE}/api/admin/requests`, { params });
  }

  updateRequestStatus(id: number, status: RequestStatus): Observable<ServiceRequestResponse> {
    const params = new HttpParams({ fromObject: { status } as any });
    return this.http.patch<ServiceRequestResponse>(`${API_BASE}/api/admin/requests/${id}/status`, null, { params });
  }

  // --------- Stats بسيطة بلا endpoint خاص ----------
  // كنجيب totals باستعمال totalElements من صفحات بحجم 1
  stats(): Observable<{
    categories: number;
    clients: number;
    requests: number;
    byStatus: Record<RequestStatus, number>;
  }> {
    const one = 1;
    const categories$ = this.categories(0, one).pipe(map(p => p.totalElements));
    const clients$    = this.clients('', 0, one).pipe(map(p => p.totalElements));
    const allReq$     = this.requests({ page: 0, size: one }).pipe(map(p => p.totalElements));

    // كل ستاتيوس بوحدو
    const statuses: RequestStatus[] = ['PENDING', 'RESPONDED', 'ACCEPTED', 'REJECTED', 'CANCELLED', 'COMPLETED'];
    const perStatus$ = statuses.map(s =>
      this.requests({ status: s, page: 0, size: one }).pipe(map(p => [s, p.totalElements] as const))
    );

    return forkJoin([categories$, clients$, allReq$, ...perStatus$]).pipe(
      map((arr) => {
        const categories = arr[0] as number;
        const clients = arr[1] as number;
        const requests = arr[2] as number;
        const byStatusEntries = arr.slice(3) as ReadonlyArray<readonly [RequestStatus, number]>;
        const byStatus = Object.fromEntries(byStatusEntries) as Record<RequestStatus, number>;
        return { categories, clients, requests, byStatus };
      })
    );
  }
}
