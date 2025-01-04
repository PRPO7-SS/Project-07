import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService); // Inject AuthService

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      console.log(error.status)
      if ((error.status === 401 && error.error?.message === 'Token required') ||
        (error.status === 403 && error.error?.message === 'Invalid or expired token')) {
        console.log('Token expired, attempting refresh');

        return authService.refreshToken().pipe(
          switchMap(() => {
            console.log('Retrying original request after token refresh');
            return next(req);
          })
        );
      }

      return throwError(() => error); // Propagate error for non-401 responses
    })
  );
};
