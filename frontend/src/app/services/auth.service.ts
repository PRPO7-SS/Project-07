import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpService } from './http.service'; // Unified HTTP service
import { catchError, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly authTokenKey = 'auth_token';

  constructor(private readonly httpService: HttpService) {}

  refreshToken(): Observable<any> {
    // Use the unified HttpService to call the backend refresh token endpoint
    return this.httpService.get<any>('userService', 'auth/refresh', {
      withCredentials: true, // Ensure cookies are sent
    });
  }

  hasAuthToken(): boolean {
    return this.getCookie(this.authTokenKey) !== null;
  }

  hasRefreshToken(): Observable<boolean> {
    // Call the backend route to check for the refresh token's existence and validity
    return this.httpService
      .get<{ valid: boolean }>('userService', 'auth/check-refresh', {
        withCredentials: true, // Ensure cookies are sent
      })
      .pipe(
        map((response) => response.valid), // Map the backend response to a boolean
        catchError(() => {
          // If an error occurs, assume the refresh token is invalid
          return of(false);
        })
      );
  }

  private getCookie(name: string): string | null {
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
      const [key, value] = cookie.trim().split('=');
      if (key === name) {
        return decodeURIComponent(value);
      }
    }
    return null;
  }
}
