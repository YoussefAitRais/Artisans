import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { map, tap } from 'rxjs/operators';
import { Observable } from 'rxjs';

type Role = 'ADMIN' | 'CLIENT' | 'ARTISAN';

export interface LoginRequest { email: string; password: string; }
export interface RegisterRequest { email: string; password: string; nom: string; prenom: string; }
export interface AuthResponse {
  token?: string; id?: number; email: string; role: Role | string; nom: string; prenom: string;
}
export interface AuthSession {
  token: string; role: Role | null; email?: string; id?: number; nom?: string; prenom?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_BASE = 'http://localhost:8091';
  private readonly TOKEN_KEY = 'token';
  private readonly ROLE_KEY  = 'role';

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  // تخزين مؤقّت فالذاكرة ملي نكونو فـ SSR
  private memoryToken: string | null = null;
  private memoryRole: Role | null = null;

  constructor(private http: HttpClient) {}

  login(body: LoginRequest): Observable<AuthSession> {
    return this.http.post<AuthResponse>(`${this.API_BASE}/api/auth/login`, body, { observe: 'response' })
      .pipe(
        map((res) => this.buildSessionFromResponse(res)),
        tap((session) => this.saveSession(session.token, session.role || undefined))
      );
  }

  registerClient(body: RegisterRequest): Observable<AuthSession> {
    return this.http.post<AuthResponse>(`${this.API_BASE}/api/auth/register-client`, body, { observe: 'response' })
      .pipe(
        map((res) => this.buildSessionFromResponse(res)),
        tap((session) => this.saveSession(session.token, session.role || undefined))
      );
  }

  registerArtisan(body: RegisterRequest): Observable<AuthSession> {
    return this.http.post<AuthResponse>(`${this.API_BASE}/api/auth/register-artisan`, body, { observe: 'response' })
      .pipe(
        map((res) => this.buildSessionFromResponse(res)),
        tap((session) => this.saveSession(session.token, session.role || undefined))
      );
  }

  // ---------- Session ----------
  saveSession(token: string, role?: string) {
    if (this.isBrowser) {
      localStorage.setItem(this.TOKEN_KEY, token);
      if (role) localStorage.setItem(this.ROLE_KEY, role as Role);
    } else {
      this.memoryToken = token;
      this.memoryRole = (role as Role) ?? (this.decodeJwt(token)?.role as Role) ?? null;
    }
  }

  logout() {
    if (this.isBrowser) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.ROLE_KEY);
    }
    this.memoryToken = null;
    this.memoryRole = null;
  }

  getToken(): string | null {
    return this.isBrowser ? localStorage.getItem(this.TOKEN_KEY) : this.memoryToken;
  }

  getRole(): Role | null {
    return this.isBrowser
      ? (localStorage.getItem(this.ROLE_KEY) as Role) || null
      : this.memoryRole;
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
    const payload = this.decodeJwt(token) || {};
    return { id: payload['id'], email: payload['sub'], role: payload['role'] as Role };
  }

  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders(token ? { Authorization: `Bearer ${token}` } : {});
  }

  // ---------- Utils ----------
  private buildSessionFromResponse(res: HttpResponse<AuthResponse>): AuthSession {
    const body = res.body || ({} as AuthResponse);
    let token = body.token;

    if (!token) {
      const auth = res.headers.get('Authorization'); // "Bearer xxx"
      const xAuth = res.headers.get('X-Auth-Token');
      if (auth && auth.toLowerCase().startsWith('bearer ')) token = auth.substring(7);
      else if (xAuth) token = xAuth;
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

  private decodeJwt(token: string): any | null {
    try {
      const [, payload] = token.split('.');
      // فـ SSR ماكاينش atob
      if (typeof atob === 'undefined') return null;
      const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decodeURIComponent(escape(json)));
    } catch {
      return null;
    }
  }
}
