import { CanMatchFn, Router, UrlTree } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export type Role = 'ADMIN' | 'CLIENT' | 'ARTISAN';

export function roleGuard(...allowed: Role[]): CanMatchFn {
  return (): boolean | UrlTree => {
    const router = inject(Router);
    const platformId = inject(PLATFORM_ID);
    const isBrowser = isPlatformBrowser(platformId);

    const role = isBrowser ? (localStorage.getItem('role') as Role | null) : null;
    return role && allowed.includes(role) ? true : router.parseUrl('/login');
  };
}
