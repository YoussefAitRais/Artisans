// src/app/register/register.component.ts
import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService, RegisterRequest, AuthSession } from '../services/auth/auth.service';

type RoleChoice = 'CLIENT' | 'ARTISAN';

export type registerForm = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmerPassword: string;
  role: RoleChoice | ''; // use CLIENT | ARTISAN to match backend
};

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
})
export class RegisterComponent {
  registerObj: registerForm = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmerPassword: '',
    role: '' // 'CLIENT' or 'ARTISAN'
  };

  loading = false;
  errorMsg = '';

  constructor(private auth: AuthService, private router: Router) {}

  setRole(role: RoleChoice) {
    this.registerObj.role = role;
  }

  // Map form → backend DTO (nom/prenom instead of firstName/lastName)
  private toRegisterRequest(): RegisterRequest {
    return {
      email: this.registerObj.email.trim(),
      password: this.registerObj.password,
      nom: this.registerObj.firstName.trim(),
      prenom: this.registerObj.lastName.trim(),
    };
  }

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

    const payload = this.toRegisterRequest();
    this.loading = true;

    const call$ = this.registerObj.role === 'CLIENT'
      ? this.auth.registerClient(payload)
      : this.auth.registerArtisan(payload);

    call$.subscribe({
      next: (session: AuthSession) => {
        // Auto-redirect by role
        if (session.role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else if (session.role === 'ARTISAN') {
          // عندك لاحقًا Dashboard ديال الحرفي، مؤقتًا نمشيو للـ client باش نكمّلو
          this.router.navigate(['/client']);
        } else {
          this.router.navigate(['/client']);
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Registration failed';
        console.error('Register error', err);
      },
      complete: () => (this.loading = false),
    });
  }
}
