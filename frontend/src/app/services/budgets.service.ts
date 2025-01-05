import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpService } from './http.service'; // Import the HttpService
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class BudgetService {
  private readonly serviceName = 'budgetService'; // Specify the microservice name

  constructor(private readonly httpService: HttpService) {}

  // Fetch all budgets for the authenticated user
  getBudgets(): Observable<any> {
    return this.httpService.get(this.serviceName, 'budget', {
      withCredentials: true, // Send authentication cookies
    });
  }

  // Add a new budget
  addBudget(budgetData: {
    category: string;
    monthlyLimit: number;
  }): Observable<any> {
    return this.httpService.post(this.serviceName, 'budget', budgetData, {
      withCredentials: true, // Send authentication cookies
    });
  }

  // Update an existing budget
  updateBudget(categoryName: string, newLimit: number): Observable<any> {
    const updatePayload = { newLimit }; // Payload for updating the budget
    return this.httpService.put(this.serviceName, `budget/${categoryName}`, updatePayload, {
      withCredentials: true,
    });
  }

  // Delete a budget by category name
  deleteBudget(categoryName: string): Observable<any> {
    return this.httpService.delete(this.serviceName, `budget/${categoryName}`, {
      withCredentials: true,
    });
  }
}