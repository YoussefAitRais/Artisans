import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.error = '';
    this.loading = true;

    this.auth.login(this.email, this.password).subscribe({
      next: res => {
        this.auth.saveToken(res.token);
        const role = this.auth.role;
        if (role === 'ARTISAN')      this.router.navigate(['/artisan/home']);
        else if (role === 'CLIENT')  this.router.navigate(['/client/home']);
        else if (role === 'ADMIN')   this.router.navigate(['/admin']);
        else                         this.router.navigate(['/']);
        this.loading = false;
      },
      error: e => {
        this.loading = false;
        this.error = e?.error?.message || 'Email ou mot de passe invalide';
      }
    });
  }
}
