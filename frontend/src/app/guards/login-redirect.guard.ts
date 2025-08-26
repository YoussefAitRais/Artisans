// src/app/guards/login-redirect.guard.ts
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router, CanMatchFn } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';

export const loginRedirectGuard: CanMatchFn = () => {
  const platformId = inject(PLATFORM_ID);
  // فـSSR ما نديروالو
  if (!isPlatformBrowser(platformId)) return true;

  const auth = inject(AuthService);
  const router = inject(Router);

  // ما مسجّلش الدخول → خليه يدخل للوجين/ريجيستر
  if (!auth.token) return true;

  // مسجّل → حوّلو لداشبورد المناسب
  const role = auth.role;
  const to =
    role === 'ARTISAN' ? '/artisan/home' :
      role === 'CLIENT'  ? '/client/home'  :
        role === 'ADMIN'   ? '/admin'        : '/';

  // مهم: نرجعو UrlTree ماشي navigate (باش ما يكونش لووب)
  return router.createUrlTree([to]);
};
