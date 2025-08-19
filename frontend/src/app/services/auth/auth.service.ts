import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { map, tap } from 'rxjs/operators';
import { Observable } from 'rxjs';

type Role = 'ADMIN' | 'CLIENT' | 'ARTISAN';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  nom: string;
  prenom: string;
}

export interface AuthResponse {
  token?: string;
  id?: number;
  email: string;
  role: Role | string;
  nom: string;
  prenom: string;
}

export interface AuthSession {
  token: string;
  role: Role | null;
  email?: string;
  id?: number;
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
      map((res) => this.buildSessionFromResponse(res)),
      tap((session) => this.saveSession(session.token, session.role || undefined))
    );
  }

  registerClient(body: RegisterRequest): Observable<AuthSession> {
    return this.http.post<AuthResponse>(
      `${this.API_BASE}/api/auth/register-client`,
      body,
      { observe: 'response' }
    ).pipe(
      map((res) => this.buildSessionFromResponse(res)),
      tap((session) => this.saveSession(session.token, session.role || undefined))
    );
  }

  registerArtisan(body: RegisterRequest): Observable<AuthSession> {
    return this.http.post<AuthResponse>(
      `${this.API_BASE}/api/auth/register-artisan`,
      body,
      { observe: 'response' }
    ).pipe(
      map((res) => this.buildSessionFromResponse(res)),
      tap((session) => this.saveSession(session.token, session.role || undefined))
    );
  }

  // ----------------- Session Helpers -----------------

  saveSession(token: string, role?: string) {
    localStorage.setItem(this.TOKEN_KEY, token);

    const finalRole = (role as Role) || (this.getClaim<string>('role') as Role) || null;
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
      email: this.getClaim<string>('sub'), // subject = email
      role: this.getClaim<Role>('role'),
    };
  }

  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders(
      token ? { Authorization: `Bearer ${token}` } : {}
    );
  }

  // ----------------- Private utils -----------------

  private buildSessionFromResponse(res: HttpResponse<AuthResponse>): AuthSession {
    const body = res.body || ({} as AuthResponse);

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

    if (!token) {
      throw new Error('No token returned from server');
    }

    const role = (body.role as Role) || (this.decodeJwt(token)?.role as Role) || null;

    return {
      token,
      role,
      id: body.id ?? this.decodeJwt(token)?.id,
      email: body.email ?? this.decodeJwt(token)?.sub,
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
      return JSON.parse(decodeURIComponent(escape(json)));
    } catch {
      return null;
    }
  }
}
