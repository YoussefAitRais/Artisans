// src/app/guards/auth.guard.ts
import { CanMatchFn, Router, UrlTree } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export const authGuard: CanMatchFn = (): boolean | UrlTree => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);
  const browser = isPlatformBrowser(platformId);

  const token = browser ? localStorage.getItem('token') : null;
  return token ? true : router.parseUrl('/login');
};
