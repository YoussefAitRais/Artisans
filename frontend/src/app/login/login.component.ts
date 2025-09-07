import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../services/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
  loading = false;
  error: string | null = null;
  private redirectTarget: string | null = null;

  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  ngOnInit(): void {
    this.redirectTarget = this.route.snapshot.queryParamMap.get('redirect');
  }

  getFieldError(fieldName: string): string | null {
    const field = this.loginForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) return `${this.getFieldLabel(fieldName)} is required`;
      if (field.errors['email']) return 'Please enter a valid email address';
      if (field.errors['minlength']) return 'Password must be at least 6 characters';
    }
    return null;
  }

  private getFieldLabel(fieldName: string): string {
    return fieldName === 'email' ? 'Email' : 'Password';
  }

  onSubmit(): void {
    if (this.loading) return;
    
    this.error = null;
    this.loginForm.markAllAsTouched();

    if (this.loginForm.invalid) {
      this.error = 'Please fix the validation errors above.';
      return;
    }

    const { email, password } = this.loginForm.value;

    this.loading = true;
    this.auth.login(email.trim(), password)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: ({ role }) => {
          const r = this.redirectTarget?.trim() ?? null;
          const safe = !!r && r.startsWith('/') && !r.startsWith('/api/') && !r.startsWith('//');
          const dest = (safe ? r : null) || this.auth.homeUrl(role);
          this.router.navigateByUrl(dest);
        },
        error: (err) => {
          console.error('Login error:', err);
          this.error = err?.error?.message || 'Login failed. Please check your credentials.';
        }
      });
  }
}
