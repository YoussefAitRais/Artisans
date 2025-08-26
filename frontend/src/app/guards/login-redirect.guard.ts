import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';

export const loginRedirectGuard: CanMatchFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // إذا راه داخل بالفعل رجّعو للدور ديالو
  if (auth.isLoggedIn()) {
    const r = auth.role;
    if (r === 'ARTISAN') router.navigate(['/artisan/home']);
    else if (r === 'CLIENT') router.navigate(['/client/home']);
    else if (r === 'ADMIN') router.navigate(['/admin/home']);
    else router.navigate(['/']);
    return false; // ما يخليش يدخل /login
  }
  return true; // خليه يشوف /login
};
