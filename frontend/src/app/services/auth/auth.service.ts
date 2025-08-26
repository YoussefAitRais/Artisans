import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

const API = 'http://localhost:8091/api/auth';

export type Role = 'ADMIN' | 'ARTISAN' | 'CLIENT';

export interface LoginResponse {
  token: string;
  role?: string | string[]; // ممكن يجي role هنا مباشرة
}

export interface ClientRegisterRequest {
  email: string;
  password: string;
  nom: string;
  prenom?: string;
}

export interface ArtisanRegisterRequest {
  email: string;
  password: string;
  nom: string;
  prenom?: string;
  metier: string;
  localisation?: string;
  description?: string;
  categoryId: number;
}

export interface AuthSession {
  token: string;
  role: Role;
  email: string;
  nom?: string;
  prenom?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private platformId = inject(PLATFORM_ID);

  constructor(private http: HttpClient) {}

  // ---------- helpers: localStorage (آمن مع SSR) ----------
  private get canStorage() {
    return isPlatformBrowser(this.platformId) && typeof localStorage !== 'undefined';
  }

  saveToken(t: string) { if (this.canStorage) localStorage.setItem('token', t); }
  get token(): string | null { return this.canStorage ? localStorage.getItem('token') : null; }

  setRole(role: Role | null) {
    if (!this.canStorage) return;
    if (role) localStorage.setItem('role', role); else localStorage.removeItem('role');
  }

  private normalizeRole(v?: string | null): Role | null {
    if (!v) return null;
    const s = v.toUpperCase();
    if (s.includes('ADMIN')) return 'ADMIN';
    if (s.includes('ARTISAN')) return 'ARTISAN';
    if (s.includes('CLIENT')) return 'CLIENT';
    return null;
  }

  private parseJwt(t: string): any {
    try {
      const p = t.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const pad = p.length % 4; const b64 = pad ? p + '='.repeat(4 - pad) : p;
      return JSON.parse(atob(b64));
    } catch { return null; }
  }

  private roleFromPayload(p: any): Role | null {
    if (!p) return null;
    const toArr = (x: any) => Array.isArray(x) ? x : (typeof x === 'string' ? x.split(/[ ,]/) : []);
    const raw = [
      ...toArr(p.roles),
      ...toArr(p.authorities),
      ...toArr(p.scope),
      ...toArr(p.scopes),
      ...toArr(p.permissions),
      p.role, p.userRole, p.user_type
    ].filter(Boolean).map((v: any) => (typeof v === 'string' ? v : v.authority ?? ''));

    const hit = (k: string) => raw.some((v: string) => {
      const s = (v || '').toUpperCase(); return s === k || s === `ROLE_${k}`;
    });

    if (hit('ADMIN')) return 'ADMIN';
    if (hit('ARTISAN')) return 'ARTISAN';
    if (hit('CLIENT')) return 'CLIENT';
    return null;
  }

  get role(): Role | null {
    if (!this.canStorage) return null;
    const stored = this.normalizeRole(localStorage.getItem('role'));
    if (stored) return stored;
    const t = this.token; if (!t) return null;
    return this.roleFromPayload(this.parseJwt(t));
  }

  isLoggedIn(): boolean { return !!this.token; }

  logout() {
    if (!this.canStorage) return;
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('email');
  }

  // ---------- API ----------
  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API}/login`, { email, password }).pipe(
      map(res => {
        this.saveToken(res.token);
        // الدور من الاستجابة أو من الـ JWT
        const raw = Array.isArray(res.role) ? res.role.join(' ') : (res.role ?? '');
        const role = this.normalizeRole(raw) || this.roleFromPayload(this.parseJwt(res.token));
        if (role) this.setRole(role);
        if (this.canStorage) localStorage.setItem('email', email);
        return res;
      })
    );
  }

  registerClient(body: ClientRegisterRequest): Observable<AuthSession> {
    return this.http.post<any>(`${API}/register-client`, body).pipe(
      map(res => {
        const token: string = res.token ?? res?.data?.token;
        const raw = Array.isArray(res.role) ? res.role.join(' ') : (res.role ?? 'CLIENT');
        const role = this.normalizeRole(raw) || this.roleFromPayload(this.parseJwt(token)) || 'CLIENT';
        this.saveToken(token);
        this.setRole(role);
        if (this.canStorage) localStorage.setItem('email', res.user?.email ?? body.email);
        return {
          token,
          role,
          email: res.user?.email ?? body.email,
          nom: res.user?.nom ?? body.nom,
          prenom: res.user?.prenom ?? body.prenom
        } as AuthSession;
      })
    );
  }

  registerArtisan(body: ArtisanRegisterRequest): Observable<AuthSession> {
    return this.http.post<any>(`${API}/register-artisan`, body).pipe(
      map(res => {
        const token: string = res.token ?? res?.data?.token;
        const raw = Array.isArray(res.role) ? res.role.join(' ') : (res.role ?? 'ARTISAN');
        const role = this.normalizeRole(raw) || this.roleFromPayload(this.parseJwt(token)) || 'ARTISAN';
        this.saveToken(token);
        this.setRole(role);
        if (this.canStorage) localStorage.setItem('email', res.user?.email ?? body.email);
        return {
          token,
          role,
          email: res.user?.email ?? body.email,
          nom: res.user?.nom ?? body.nom,
          prenom: res.user?.prenom ?? body.prenom
        } as AuthSession;
      })
    );
  }
}
