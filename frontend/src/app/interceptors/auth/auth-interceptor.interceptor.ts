import { HttpInterceptorFn } from '@angular/common/http';

const API_BASE = 'http://localhost:8091';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const token = localStorage.getItem('token');

  const isApiCall =
    req.url.startsWith(API_BASE) ||
    req.url.startsWith('/api') ||
    req.url.includes('localhost:8091');

  if (token && isApiCall) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
