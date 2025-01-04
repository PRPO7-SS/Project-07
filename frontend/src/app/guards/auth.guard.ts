import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Check for auth token (synchronous check)
  if (authService.hasAuthToken()) {
    console.log('Auth token found');
    return true;
  }

  // Check for refresh token (asynchronous check)
  return authService.hasRefreshToken().pipe(
    map((isValid) => {
      if (isValid) {
        console.log('Valid refresh token found');
        return true; // Allow navigation
      }

      console.log('No valid refresh token, redirecting to login');
      router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false; // Block navigation
    }),
    catchError(() => {
      console.log('Error during refresh token check, redirecting to login');
      router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return of(false); // Block navigation on error
    })
  );
};
