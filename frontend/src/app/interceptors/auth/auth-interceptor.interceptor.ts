import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformServer } from '@angular/common';

const API_BASE = 'http://localhost:8091';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platformId = inject(PLATFORM_ID);
  if (isPlatformServer(platformId)) {
    return next(req);
  }

  const token = localStorage.getItem('token');
  const isApiCall =
    req.url.startsWith(API_BASE) ||
    req.url.startsWith('/api') ||
    req.url.includes('localhost:8091');

  if (token && isApiCall) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
