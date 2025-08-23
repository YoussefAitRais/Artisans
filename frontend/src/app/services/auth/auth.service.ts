import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';

// --------- Types ---------
export type Role = 'ADMIN' | 'CLIENT' | 'ARTISAN';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ClientRegisterRequest {
  email: string;
  password: string;
  nom: string;
  prenom: string;
}

export interface ArtisanRegisterRequest {
  email: string;
  password: string;
  nom: string;
  prenom: string;
  // حقول إضافية للحرفي
  categoryId: number;
  metier?: string;
  localisation?: string;
  description?: string;
}

export interface AuthResponse {
  token?: string;
  id?: number;
  email: string;
  role: Role | string;
  nom?: string;
  prenom?: string;
}

export interface AuthSession {
  token: string;
  role: Role | null;
  id?: number;
  email?: string;
  nom?: string;
  prenom?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_BASE = 'http://localhost:8091';

  private readonly TOKEN_KEY = 'token';
  private readonly ROLE_KEY  = 'role';

  constructor(private http: HttpClient) {}

  // ----------------- Auth Calls -----------------

  login(body: LoginRequest): Observable<AuthSession> {
    return this.http.post<AuthResponse>(
      `${this.API_BASE}/api/auth/login`,
      body,
      { observe: 'response' }
    ).pipe(
      map((res: HttpResponse<AuthResponse>) => this.buildSessionFromResponse(res)),
      tap((session: AuthSession) => this.saveSession(session.token, session.role || undefined))
    );
  }

  registerClient(body: ClientRegisterRequest): Observable<AuthSession> {
    return this.http.post<AuthResponse>(
      `${this.API_BASE}/api/auth/register-client`,
      body,
      { observe: 'response' }
    ).pipe(
      map((res: HttpResponse<AuthResponse>) => this.buildSessionFromResponse(res)),
      tap((session: AuthSession) => this.saveSession(session.token, session.role || undefined))
    );
  }

  registerArtisan(body: ArtisanRegisterRequest): Observable<AuthSession> {
    return this.http.post<AuthResponse>(
      `${this.API_BASE}/api/auth/register-artisan`,
      body,
      { observe: 'response' }
    ).pipe(
      map((res: HttpResponse<AuthResponse>) => this.buildSessionFromResponse(res)),
      tap((session: AuthSession) => this.saveSession(session.token, session.role || undefined))
    );
  }

  // ----------------- Session Helpers -----------------

  saveSession(token: string, role?: string | Role) {
    localStorage.setItem(this.TOKEN_KEY, token);

    const finalRole: Role | null =
      (role as Role) ||
      (this.getClaim<string>('role') as Role) ||
      null;

    if (finalRole) localStorage.setItem(this.ROLE_KEY, finalRole);
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.ROLE_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRole(): Role | null {
    return (localStorage.getItem(this.ROLE_KEY) as Role) || null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  hasRole(role: Role): boolean {
    return this.getRole() === role;
  }

  getCurrentUser() {
    const token = this.getToken();
    if (!token) return null;
    return {
      id: this.getClaim<number>('id'),
      email: this.getClaim<string>('sub'),
      role: this.getClaim<Role>('role'),
    };
  }

  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders(token ? { Authorization: `Bearer ${token}` } : {});
  }

  // ----------------- Private utils -----------------

  private buildSessionFromResponse(res: HttpResponse<AuthResponse>): AuthSession {
    const body = res.body || ({} as AuthResponse);

    // token من body أو من Header
    let token = body.token;
    if (!token) {
      const auth = res.headers.get('Authorization'); // "Bearer xxx"
      const xAuth = res.headers.get('X-Auth-Token');
      if (auth && auth.toLowerCase().startsWith('bearer ')) {
        token = auth.substring(7);
      } else if (xAuth) {
        token = xAuth;
      }
    }
    if (!token) throw new Error('No token returned from server');

    const decoded = this.decodeJwt(token) || {};
    const role = (body.role as Role) || (decoded.role as Role) || null;

    return {
      token,
      role,
      id: body.id ?? decoded.id,
      email: body.email ?? decoded.sub,
      nom: body.nom,
      prenom: body.prenom,
    };
  }

  private getClaim<T = unknown>(name: string): T | null {
    const token = this.getToken();
    if (!token) return null;
    const payload = this.decodeJwt(token);
    return (payload && (payload as any)[name]) ?? null;
  }

  private decodeJwt(token: string): any | null {
    try {
      const [, payload] = token.split('.');
      const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      // decodeURIComponent(escape(...)) كان كيدير مشاكل فبعض الحالات، نخلوها بسيطة
      return JSON.parse(json);
    } catch {
      return null;
    }
  }
}
