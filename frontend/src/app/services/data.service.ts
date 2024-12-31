import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { User } from '../models/user';
import { HttpService } from './http.service';
import { HttpParams } from '@angular/common/http';
import { RegisterRequest } from '../models/registerRequest';


@Injectable({
  providedIn: 'root',
})
export class DataService {
  constructor(private readonly httpService: HttpService) {}

  // Login method
  login(credentials: { email: string; password: string }): Observable<any> {
    return this.httpService.post<any>('userService', 'auth/login', credentials, { withCredentials: true });
  }

  // Refactored method: Get registration data
  getRegisterData(): Observable<any> {
    return this.httpService.get<any>('userService','register', { withCredentials: true });
  }

  // New method for registration
  register(payload: RegisterRequest): Observable<any> {
    return this.httpService.post<any>('userService','auth/register', payload);
  }

  // Refactored method: Get current user profile
  getCurrentUser(): Observable<User> {
    return this.httpService.get<User>('userService','users/profile', { withCredentials: true });
  }

  deleteCurrentUser(): Observable<any> {
    return this.httpService.delete<User>('userService','users/profile', { withCredentials: true });
  }

  updateCurrentUser(payload: User): Observable<User> {
    return this.httpService.put<User>('userService','users/profile', payload, { withCredentials: true });
  }

  getUserAge(): Observable<{ age: number }> {
    return this.httpService.get<{ age: number }>('userService','users/age', { withCredentials: true });
  }

  updatePassword(payload: Object): Observable<any> {
    return this.httpService.put<User>('userService','users/change-password', payload, { withCredentials: true });
  }

  logout(): Observable<any> {
    return this.httpService.post<any>('userService','auth/logout', {}, { withCredentials: true });
  }






}
