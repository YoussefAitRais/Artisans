import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

const API_BASE = 'http://localhost:8091';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platformId = inject(PLATFORM_ID);
  const can = isPlatformBrowser(platformId) && typeof localStorage !== 'undefined';

  // public categories بدون توكن
  const isPublicCategories = req.method === 'GET' && /\/api\/categories(\/.*)?$/.test(req.url);
  if (isPublicCategories) return next(req);

  const token = can ? localStorage.getItem('token') : null;
  const isApi = req.url.startsWith(API_BASE) || req.url.includes('localhost:8091');

  if (token && isApi) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
