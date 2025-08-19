import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService, LoginRequest, AuthSession } from '../services/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  loginObj = {
    email: '',
    password: ''
  };

  loading = false;
  errorMsg = '';

  constructor(private auth: AuthService, private router: Router) {}

  onLoginSubmit(form: NgForm) {
    this.errorMsg = '';

    if (!form.valid) {
      this.errorMsg = 'Please fill in all fields';
      return;
    }

    const payload: LoginRequest = {
      email: this.loginObj.email.trim(),
      password: this.loginObj.password
    };

    this.loading = true;

    this.auth.login(payload).subscribe({
      next: (session: AuthSession) => {
        // توجيه حسب الدور
        switch (session.role) {
          case 'ARTISAN':
            this.router.navigate(['/artisan/dashboard']);
            break;
          case 'CLIENT':
            this.router.navigate(['/client/home']);
            break;
          case 'ADMIN':
            this.router.navigate(['/admin']);
            break;
          default:
            this.router.navigate(['/']);
        }
      },
      error: (err) => {
        console.error('Login error', err);
        this.errorMsg = err?.error?.message || 'Email أو Password غير صحيح';
        this.loading = false;
      },
      complete: () => (this.loading = false)
    });
  }
}
