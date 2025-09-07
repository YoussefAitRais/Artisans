import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

export type Role = 'ADMIN' | 'CLIENT' | 'ARTISAN';

export interface LoginResponse {
  token?: string;
  access_token?: string;
  accessToken?: string;
  role: Role;
}

export interface RegisterRequest {
  nom: string;           // Backend expects 'nom' not 'firstName'
  prenom: string;        // Backend expects 'prenom' not 'lastName'
  email: string;
  password: string;
  role: Role;
  categoryId?: number | null;
  metier?: string | null;
  localisation?: string | null;
  description?: string | null;
}

const API = 'http://localhost:8091/api';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<{ token: string; role: Role }> {
    return this.http.post<LoginResponse>(`${API}/auth/login`, { email, password }).pipe(
      map(res => {
        const token = res.token || res.access_token || (res as any).accessToken;
        if (!token || !res.role) throw new Error('Invalid login response');
        localStorage.setItem('token', token);
        localStorage.setItem('role', res.role);
        return { token, role: res.role };
      })
    );
  }

  register(body: RegisterRequest): Observable<any> {
    const path =
      body.role === 'ARTISAN' ? 'register-artisan' :
        body.role === 'CLIENT'  ? 'register-client'  : null;
    if (!path) throw new Error('role must be CLIENT or ARTISAN');

    // Prepare payload according to backend DTO structure
    const payload: any = {
      nom: body.nom,
      prenom: body.prenom,
      email: body.email,
      password: body.password
    };
    
    // Add artisan-specific fields if needed
    if (body.role === 'ARTISAN') {
      payload.metier = body.metier;
      payload.localisation = body.localisation;
      payload.description = body.description;
      payload.categoryId = body.categoryId;
    }
    
    return this.http.post(`${API}/auth/${path}`, payload);
  }

  // === اللي طالبو login.component.ts
  homeUrl(role: Role | null): string {
    if (role === 'ADMIN') return '/admin/home';
    if (role === 'ARTISAN') return '/artisan/home';
    return '/client/home';
  }

  get token(): string | null { return localStorage.getItem('token'); }
  get role(): Role | null { return (localStorage.getItem('role') as Role) ?? null; }

  logout() { localStorage.removeItem('token'); localStorage.removeItem('role'); }
}
