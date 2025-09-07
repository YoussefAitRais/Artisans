// src/app/guards/auth.guard.spec.ts
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { authGuard } from './auth.guard';

describe('authGuard (CanActivate)', () => {
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([])],
    });
    router = TestBed.inject(Router);
    localStorage.removeItem('access_token');
  });

  const call = (url: string) => {
    const route = {} as ActivatedRouteSnapshot;
    const state = { url } as RouterStateSnapshot;
    // @ts-ignore
    return TestBed.runInInjectionContext(() => authGuard(route, state));
  };

  it('allows /login without token', () => {
    const res = call('/login');
    expect(res).toBeTrue();
  });

  it('redirects protected pages to /login without token', () => {
    const res = call('/artisan');
    expect(res instanceof UrlTree).toBeTrue();
    expect(router.serializeUrl(res as UrlTree)).toBe('/login');
  });

  it('redirects token user away from /login', () => {
    localStorage.setItem('access_token', 't');
    const res = call('/login');
    expect(res instanceof UrlTree).toBeTrue();
    // adjust to your actual post-login route if different
    // expect(router.serializeUrl(res as UrlTree)).toBe('/client/home');
    localStorage.removeItem('access_token');
  });
});
