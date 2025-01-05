import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpService } from './http.service'; // Import the HttpService
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class DebtService {
  private readonly serviceName = 'debtService'; // Specify the microservice name

  constructor(private readonly httpService: HttpService) {}

  // Fetch all debts for the authenticated user
  getDebts(): Observable<any> {
    return this.httpService.get(this.serviceName, 'debts', {
      withCredentials: true, // Send authentication cookies
    });
  }

  // Add a new debt
  addDebt(debtData: {
    creditor: string;
    description: string; // Required description
    amount: number;
    deadline: string; // Required deadline
    isPaid: boolean; // Paid status
  }): Observable<any> {
    // Preverjanje obveznih polj
    if (!debtData.creditor) {
      throw new Error("Creditor is required.");
    }
  
    if (!debtData.description) {
      throw new Error("Description is required.");
    }
  
    if (!debtData.deadline) {
      throw new Error("Deadline is required.");
    }
  
    return this.httpService.post(this.serviceName, 'debts', debtData, {
      withCredentials: true, // Send authentication cookies
    });
  }

  // Update an existing debt
  updateDebt(debtId: string, updatedDebtData: any): Observable<any> {
    return this.httpService.put(this.serviceName, `debts/${debtId}`, updatedDebtData, {
      withCredentials: true,
    });
  }

  // Delete a debt by its ID
  deleteDebt(debtId: string): Observable<any> {
    return this.httpService.delete(this.serviceName, `debts/${debtId}`, {
      withCredentials: true,
    });
  }

  // Mark a debt as paid
  markDebtAsPaid(debtId: string): Observable<any> {
    return this.httpService.put(this.serviceName, `debts/${debtId}/markAsPaid`, {}, {
      withCredentials: true,
    });
  }
}