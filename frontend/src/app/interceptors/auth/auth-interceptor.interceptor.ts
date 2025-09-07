import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // تجنّب إضافة Authorization على auth endpoints (نسبي أو مطلق)
  const isAuth = /\/api\/auth\//.test(req.url);
  if (isAuth) return next(req);

  const token = localStorage.getItem('token');
  if (!token) return next(req);

  const cloned = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  return next(cloned);
};
