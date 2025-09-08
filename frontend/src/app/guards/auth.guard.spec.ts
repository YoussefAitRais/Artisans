// src/app/guards/auth.guard.spec.ts
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { authCanMatch } from './auth.guard';

describe('authCanMatch (CanMatch)', () => {
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([])],
    });
    router = TestBed.inject(Router);
    localStorage.removeItem('token'); // Use 'token' to match the guard
  });

  const call = () => {
    const route = {} as any;
    const state = {} as any;
    return TestBed.runInInjectionContext(() => authCanMatch(route, state));
  };

  it('allows access when token exists', () => {
    localStorage.setItem('token', 'valid-token');
    const res = call();
    expect(res).toBeTrue();
    localStorage.removeItem('token');
  });

  it('redirects to login when no token', () => {
    const res = call();
    expect(res instanceof UrlTree).toBeTrue();
    expect(router.serializeUrl(res as UrlTree)).toBe('/login');
  });

  it('redirects to login when token exists but is empty', () => {
    localStorage.setItem('token', '');
    const res = call();
    expect(res instanceof UrlTree).toBeTrue();
    expect(router.serializeUrl(res as UrlTree)).toBe('/login');
    localStorage.removeItem('token');
  });
});
