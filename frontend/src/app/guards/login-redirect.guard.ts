import { CanMatchFn, Router, UrlTree } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export const loginRedirectGuard: CanMatchFn = (): boolean | UrlTree => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);
  const isBrowser = isPlatformBrowser(platformId);

  const role = isBrowser ? (localStorage.getItem('role') as 'ADMIN'|'CLIENT'|'ARTISAN'|null) : null;

  if (!role) return true;
  if (role === 'ADMIN')   return router.parseUrl('/admin/home');
  if (role === 'ARTISAN') return router.parseUrl('/artisan/home');
  return router.parseUrl('/client/home');
};
