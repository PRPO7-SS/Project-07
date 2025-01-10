import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { HttpService } from './http.service'; // Import the HttpService
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class SavingsGoalService {
  private readonly endpoint = 'savings-goals';

  constructor(private readonly httpService: HttpService) {}

  getSavingsGoals(): Observable<any> {
    return this.httpService.get( this.endpoint, { withCredentials: true }).pipe(
      catchError((error) => {
        return of([]); // Return an empty array on error
      })
    );
  }

  addSavingsGoal(savingsGoalData: {
    goalName: string;
    targetAmount: number;
    currentAmount: number;
    startDate: string;
    deadline: string;
  }): Observable<any> {
    return this.httpService.post(this.endpoint, savingsGoalData, {
      withCredentials: true, // Send authentication cookies
    });
  }

  updateSavingsGoal(goalId: string, updateData: any): Observable<any> {
    return this.httpService.put(`${this.endpoint}/${goalId}`, updateData, {
      withCredentials: true,
    });
  }

  deleteSavingsGoal( goalId: string): Observable<any> {
    return this.httpService.delete( `${this.endpoint}/${goalId}`, {
      withCredentials: true,
    });
  }

}
