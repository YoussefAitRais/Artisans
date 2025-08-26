// src/app/guards/role.guard.ts
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router, CanMatchFn } from '@angular/router';
import { AuthService, Role } from '../services/auth/auth.service';

export const roleGuard = (expected: Role): CanMatchFn => () => {
  const platformId = inject(PLATFORM_ID);
  if (!isPlatformBrowser(platformId)) return true;

  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.token) return router.createUrlTree(['/login']);
  return auth.role === expected ? true : router.createUrlTree(['/login']);
};
