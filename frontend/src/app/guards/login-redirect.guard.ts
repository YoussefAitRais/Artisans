// src/app/guards/login-redirect.guard.ts
import { CanMatchFn, Router, UrlTree } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export const loginRedirectGuard: CanMatchFn = (): boolean | UrlTree => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);
  const browser = isPlatformBrowser(platformId);

  const token = browser ? localStorage.getItem('token') : null;
  const role  = browser ? localStorage.getItem('role')  : null;

  if (!token) return true; // ما مسجّلش → خلّيه يدخل login/register

  // مسجّل → رجّعو للدّاشبورد ديالو
  if (role === 'ADMIN')   return router.parseUrl('/admin');
  if (role === 'ARTISAN') return router.parseUrl('/artisan');
  return router.parseUrl('/client');
};
