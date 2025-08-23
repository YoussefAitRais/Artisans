import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import {
  AuthService,
  ClientRegisterRequest,
  ArtisanRegisterRequest,
  AuthSession
} from '../services/auth/auth.service';

type RoleChoice = 'CLIENT' | 'ARTISAN';

interface Category {
  id: number;
  name: string;
  description?: string;
}

interface Page<T> {
  content: T[];
  totalElements: number;
}

type RegisterFormModel = {
  // common
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmerPassword: string;
  role: RoleChoice | '';

  // artisan-only
  categoryId: number | null;
  metier?: string;
  localisation?: string;
  description?: string;
};

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
})
export class RegisterComponent implements OnInit {
  // ------------ state ------------
  registerObj: RegisterFormModel = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmerPassword: '',
    role: '',
    categoryId: null,
    metier: '',
    localisation: '',
    description: '',
  };

  categories: Category[] = [];
  loading = false;
  errorMsg = '';

  // ------------ services ------------
  private auth = inject(AuthService);
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly API_BASE = 'http://localhost:8091';

  // ------------ lifecycle ------------
  ngOnInit() {
    // نقدر نحمّلها مباشرة أو بعد اختيار ARTISAN
    this.loadCategories();
  }

  // ------------ helpers ------------
  setRole(role: RoleChoice) {
    this.registerObj.role = role;

    if (role === 'CLIENT') {
      this.registerObj.categoryId = null;
      this.registerObj.metier = '';
      this.registerObj.localisation = '';
      this.registerObj.description = '';
    } else if (role === 'ARTISAN' && this.categories.length === 0) {
      this.loadCategories();
    }
  }

  private toClientPayload(): ClientRegisterRequest {
    return {
      email: this.registerObj.email.trim(),
      password: this.registerObj.password,
      nom: this.registerObj.firstName.trim(),
      prenom: this.registerObj.lastName.trim(),
    };
  }

  private toArtisanPayload(): ArtisanRegisterRequest {
    return {
      email: this.registerObj.email.trim(),
      password: this.registerObj.password,
      nom: this.registerObj.firstName.trim(),
      prenom: this.registerObj.lastName.trim(),
      metier: (this.registerObj.metier || '').trim(),
      localisation: (this.registerObj.localisation || '').trim() || undefined,
      description: (this.registerObj.description || '').trim() || undefined,
      categoryId: Number(this.registerObj.categoryId),
    };
  }

  private loadCategories() {
    this.http.get<Page<Category>>(`${this.API_BASE}/api/categories`).subscribe({
      next: (res) => {
        this.categories = res?.content ?? [];
      },
      error: (e) => {
        console.error('Categories load failed', e);
        this.categories = [];
      }
    });
  }

  // ------------ submit ------------
  onRegisterSubmit(form: NgForm) {
    this.errorMsg = '';

    if (!this.registerObj.role) {
      this.errorMsg = 'Please choose a role: CLIENT or ARTISAN.';
      return;
    }

    if (!form.valid) {
      this.errorMsg = 'Please fill all required fields.';
      return;
    }

    if (this.registerObj.password !== this.registerObj.confirmerPassword) {
      this.errorMsg = 'Passwords do not match.';
      return;
    }

    if (this.registerObj.role === 'ARTISAN') {
      if (this.registerObj.categoryId == null) {
        this.errorMsg = 'Please choose a category.';
        return;
      }
      if (!this.registerObj.metier || !this.registerObj.metier.trim()) {
        this.errorMsg = 'Please enter your craft (metier).';
        return;
      }
    }

    this.loading = true;

    const req$ =
      this.registerObj.role === 'CLIENT'
        ? this.auth.registerClient(this.toClientPayload())
        : this.auth.registerArtisan(this.toArtisanPayload());

    req$.subscribe({
      next: (session: AuthSession) => {
        if (session.role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else if (session.role === 'ARTISAN') {
          this.router.navigate(['/client']);
        } else {
          this.router.navigate(['/client']);
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg =
          err?.error?.message ||
          err?.message ||
          'Registration failed. Please try again.';
        console.error('Register error', err);
      },
      complete: () => (this.loading = false),
    });
  }
}
