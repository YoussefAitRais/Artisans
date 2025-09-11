import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService, Role } from '../services/auth/auth.service';

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
    // Step 1: Prevent multiple submissions
    if (this.loading) {
      console.log('Login already in progress, ignoring duplicate submission');
      return;
    }

    // Step 2: Clear any previous error messages
    this.error = null;
    this.loginForm.markAllAsTouched();

    // Step 3: Validate form before sending request
    if (this.loginForm.invalid) {
      this.error = 'Please fix the validation errors above.';
      console.log('❌ Form validation failed');
      return;
    }

    // Step 4: Extract form data and prepare for login
    const { email, password } = this.loginForm.value;

    if (!email || !password) {
      this.error = 'Email and password are required.';
      return;
    }

    console.log('🚀 Starting login process for:', email);
    this.loading = true;

    // Step 5: Send login request with proper error handling
    this.auth.login(email.trim(), password)
      .pipe(
        finalize(() => {
          this.loading = false;
          console.log('🏁 Login process completed');
        })
      )
      .subscribe({
        next: ({ role }) => {
          console.log('Login successful! Redirecting user with role:', role);

          // Determine where to redirect the user
          const redirectPath = this.getRedirectPath(role);
          console.log('Redirecting to:', redirectPath);

          this.router.navigateByUrl(redirectPath);
        },
        error: (err) => {
          console.error('Login failed:', err);

          // Use the improved error message from AuthService
          this.error = err?.error?.message || 'Login failed. Please check your credentials and try again.';
        }
      });
  }


  private getRedirectPath(role: Role): string {
    // First check if there's a specific redirect target
    const requestedRedirect = this.redirectTarget?.trim();

    if (requestedRedirect) {
      // Validate redirect target is safe
      const isSafeRedirect = requestedRedirect.startsWith('/') &&
                            !requestedRedirect.startsWith('/api/') &&
                            !requestedRedirect.startsWith('//');

      if (isSafeRedirect) {
        return requestedRedirect;
      }
    }

    // Fall back to role-based home page
    return this.auth.homeUrl(role);
  }
}
