import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class HttpService {
  private readonly apiGatewayUrl = 'http://localhost:30000'; // Replace with your API Gateway URL if different

  constructor(private readonly http: HttpClient) {}

  get<T>(
    endpoint: string,
    options?: {
      params?: HttpParams;
      headers?: HttpHeaders;
      withCredentials?: boolean;
    }
  ): Observable<T> {
    const url = `${this.apiGatewayUrl}/${endpoint}`;
    return this.http.get<T>(url, options).pipe(catchError((error) => this.handleError(error, url)));
  }

  post<T>(
    endpoint: string,
    body: any,
    options?: {
      headers?: HttpHeaders;
      withCredentials?: boolean;
    }
  ): Observable<T> {
    const url = `${this.apiGatewayUrl}/${endpoint}`;
    return this.http.post<T>(url, body, options).pipe(catchError((error) => this.handleError(error, url)));
  }

  put<T>(
    endpoint: string,
    body: any,
    options?: {
      headers?: HttpHeaders;
      withCredentials?: boolean;
    }
  ): Observable<T> {
    const url = `${this.apiGatewayUrl}/${endpoint}`;
    return this.http.put<T>(url, body, options).pipe(catchError((error) => this.handleError(error, url)));
  }

  delete<T>(
    endpoint: string,
    options?: {
      headers?: HttpHeaders;
      withCredentials?: boolean;
    }
  ): Observable<T> {
    const url = `${this.apiGatewayUrl}/${endpoint}`;
    return this.http.delete<T>(url, options).pipe(catchError((error) => this.handleError(error, url)));
  }

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
