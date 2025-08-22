// src/app/guards/role.guard.ts
import { CanMatchFn, Router, UrlTree } from '@angular/router';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

type Role = 'ADMIN' | 'CLIENT' | 'ARTISAN';

export function roleGuard(expected: Role): CanMatchFn {
  return (): boolean | UrlTree => {
    const router = inject(Router);
    const platformId = inject(PLATFORM_ID);
    const browser = isPlatformBrowser(platformId);

    const role = browser ? localStorage.getItem('role') as Role | null : null;

    if (role === expected) return true;

    // إذا داخل ولكن برول آخر، رجّعو للدّاشبورد ديالو
    if (role === 'ADMIN') return router.parseUrl('/admin');
    if (role === 'ARTISAN') return router.parseUrl('/artisan');
    if (role === 'CLIENT') return router.parseUrl('/client');

    // ما داخلش
    return router.parseUrl('/login');
  };
}
