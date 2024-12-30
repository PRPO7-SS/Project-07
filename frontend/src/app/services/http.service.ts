import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class HttpService {
  // Define microservice base URLs
  private readonly microserviceUrls = {
    userService: 'http://localhost:8080',
    transactionService: 'http://localhost:8082',
    investmentService: 'http://localhost:8083',
  };

  constructor(private readonly http: HttpClient) {}

  // Helper method to get the base URL for a microservice
  private getBaseUrl(service: keyof typeof this.microserviceUrls): string {
    return this.microserviceUrls[service];
  }

  // Unified GET method
  get<T>(
    service: keyof typeof this.microserviceUrls,
    endpoint: string,
    options?: {
      params?: HttpParams;
      headers?: HttpHeaders;
      withCredentials?: boolean;
    }
  ): Observable<T> {
    const url = `${this.getBaseUrl(service)}/${endpoint}`;
    return this.http.get<T>(url, options).pipe(catchError((error) => this.handleError(error, url)));
  }

  // Unified POST method
  post<T>(
    service: keyof typeof this.microserviceUrls,
    endpoint: string,
    body: any,
    options?: {
      headers?: HttpHeaders;
      withCredentials?: boolean;
    }
  ): Observable<T> {
    const url = `${this.getBaseUrl(service)}/${endpoint}`;
    return this.http.post<T>(url, body, options).pipe(catchError((error) => this.handleError(error, url)));
  }

  // Unified PUT method
  put<T>(
    service: keyof typeof this.microserviceUrls,
    endpoint: string,
    body: any,
    options?: {
      headers?: HttpHeaders;
      withCredentials?: boolean;
    }
  ): Observable<T> {
    const url = `${this.getBaseUrl(service)}/${endpoint}`;
    return this.http.put<T>(url, body, options).pipe(catchError((error) => this.handleError(error, url)));
  }

  // Unified DELETE method
  delete<T>(
    service: keyof typeof this.microserviceUrls,
    endpoint: string,
    options?: {
      headers?: HttpHeaders;
      withCredentials?: boolean;
    }
  ): Observable<T> {
    const url = `${this.getBaseUrl(service)}/${endpoint}`;
    return this.http.delete<T>(url, options).pipe(catchError((error) => this.handleError(error, url)));
  }

  // Handle HTTP Errors
  private handleError(error: HttpErrorResponse, endpoint: string): Observable<never> {
    let customError;
    if (error.status === 0) {
      customError = { status: 0, message: 'No connection', originalError: error };
    } else if (error.status === 404) {
      customError = { status: 404, message: `Resource not found at ${endpoint}`, originalError: error };
    } else {
      customError = { status: error.status, message: error.message || 'An unexpected error occurred', originalError: error };
    }
    return throwError(() => customError);
  }
}
