// src/app/guards/auth.guard.ts
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router, CanMatchFn } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';

export const authGuard: CanMatchFn = () => {
  const platformId = inject(PLATFORM_ID);
  if (!isPlatformBrowser(platformId)) return true;

  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.token ? true : router.createUrlTree(['/login']);
};
