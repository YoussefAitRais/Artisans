import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { map, Observable, catchError, throwError } from 'rxjs';

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

// API Configuration - Easy to change if backend URL changes
const API_BASE = 'http://localhost:8091/api';
const API_TIMEOUT = 10000; // 10 seconds timeout

/**
 * AuthService - Clean Code for Beginners
 *
 * This service handles all authentication operations.
 * It demonstrates:
 * - Clear error handling with user-friendly messages
 * - Proper HTTP error management
 * - Simple and clean method structure
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient) {}


  login(email: string, password: string): Observable<{ token: string; role: Role }> {
    console.log(' Attempting login for:', email);

    return this.http.post<LoginResponse>(`${API_BASE}/auth/login`, { email, password }).pipe(
      map(response => {
        console.log('Login response received:', response);

        // Extract token from different possible field names
        const token = response.token || response.access_token || (response as any).accessToken;

        if (!token || !response.role) {
          throw new Error('Invalid server response - missing token or role');
        }

        // Store authentication data
        localStorage.setItem('token', token);
        localStorage.setItem('role', response.role);

        console.log('Login successful for role:', response.role);
        return { token, role: response.role };
      }),
      catchError(this.handleLoginError)
    );
  }


  private handleLoginError = (error: HttpErrorResponse): Observable<never> => {
    console.error('Login error details:', error);

    let userMessage = 'Login failed. Please try again.';

    if (error.error === 0) {
      // Network error - server not reachable
      userMessage = 'Cannot connect to server. Please check:\n' +
                   '1. Is the backend server running on http://localhost:8091?\n' +
                   '2. Check your internet connection\n' +
                   '3. Try refreshing the page';
    } else if (error.status === 401) {
      // Unauthorized - wrong credentials
      userMessage = 'Invalid email or password. Please check your credentials and try again.';
    } else if (error.status === 404) {
      // Not found - wrong API endpoint
      userMessage = 'Login service not found. Please contact support.';
    } else if (error.status === 500) {
      // Server error
      userMessage = 'Server error occurred. Please try again later or contact support.';
    } else if (error.status === 0 || error.message?.includes('Failed to fetch')) {
      // CORS or network issues
      userMessage = 'Connection failed. Please ensure:\n' +
                   '• Backend server is running\n' +
                   '• CORS is properly configured\n' +
                   '• No firewall blocking the connection';
    }

    return throwError(() => ({ error: { message: userMessage } }));
  };


  register(body: RegisterRequest): Observable<any> {
    console.log('Attempting registration for:', body.email, 'as', body.role);

    // Determine the correct registration endpoint based on role
    const path = body.role === 'ARTISAN' ? 'register-artisan' :
                 body.role === 'CLIENT'  ? 'register-client'  : null;

    if (!path) {
      throw new Error('Invalid role: must be CLIENT or ARTISAN');
    }

    // Prepare payload according to backend DTO structure
    const payload: any = {
      nom: body.nom,
      prenom: body.prenom,
      email: body.email,
      password: body.password
    };

    // Add artisan-specific fields if registering as artisan
    if (body.role === 'ARTISAN') {
      payload.metier = body.metier;
      payload.localisation = body.localisation;
      payload.description = body.description;
      payload.categoryId = body.categoryId;
    }

    return this.http.post(`${API_BASE}/auth/${path}`, payload).pipe(
      catchError(this.handleRegistrationError)
    );
  }


  private handleRegistrationError = (error: HttpErrorResponse): Observable<never> => {
    console.error('Registration error:', error);

    let userMessage = 'Registration failed. Please try again.';

    if (error.status === 409) {
      userMessage = 'Email address is already registered. Please use a different email or try logging in.';
    } else if (error.status === 400) {
      userMessage = 'Invalid registration data. Please check all fields and try again.';
    } else if (error.status === 0 || error.message?.includes('Failed to fetch')) {
      userMessage = 'Cannot connect to server. Please ensure the backend is running.';
    }

    return throwError(() => ({ error: { message: userMessage } }));
  };

  // === UTILITY METHODS ===


  homeUrl(role: Role | null): string {
    switch (role) {
      case 'ADMIN': return '/admin/home';
      case 'ARTISAN': return '/artisan/home';
      case 'CLIENT': return '/client/home';
      default: return '/client/home'; // Default fallback
    }
  }


  get token(): string | null {
    return localStorage.getItem('token');
  }


  get role(): Role | null {
    return (localStorage.getItem('role') as Role) ?? null;
  }


  isLoggedIn(): boolean {
    return !!this.token && !!this.role;
  }


  logout(): void {
    console.log('Logging out user');
    localStorage.removeItem('token');
    localStorage.removeItem('role');
  }


  testConnection(): Observable<any> {
    console.log('Testing connection to backend...');
    return this.http.get(`${API_BASE}/auth/test`).pipe(
      catchError((error) => {
        console.error('Connection test failed:', error);
        return throwError(() => error);
      })
    );
  }
}


export function checkBackendConnection(): Promise<boolean> {
  return fetch(`${API_BASE}/auth/test`, {
    method: 'GET'
  })
    .then(() => {
      console.log('Backend connection successful');
      return true;
    })
    .catch((error) => {
      console.error('Backend connection failed:', error);
      return false;
    });
}
