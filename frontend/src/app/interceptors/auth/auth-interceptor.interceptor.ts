import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // تجنّب إضافة Authorization على auth endpoints (نسبي أو مطلق)
  const isAuth = /\/api\/auth\//.test(req.url);
  if (isAuth) return next(req);

  // Check if we're in browser environment to avoid SSR issues
  const platformId = inject(PLATFORM_ID);
  if (!isPlatformBrowser(platformId)) {
    return next(req);
  }

  const token = localStorage.getItem('token');
  if (!token) return next(req);

  const cloned = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  return next(cloned);
};
