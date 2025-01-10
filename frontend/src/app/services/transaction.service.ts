import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpService } from './http.service'; // Import the HttpService
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class TransactionService {

  constructor(private readonly httpService: HttpService) {}

  // Fetch all user transactions
  getUserTransactions(): Observable<any> {
    return this.httpService.get('transactions', {
      withCredentials: true, // Send authentication cookies
    });
  }

  // Add a new transaction
  addTransaction(transactionData: {
    type: string;
    amount: number;
    category: string;
    date: string;
  }): Observable<any> {
    return this.httpService.post('transactions', transactionData, {
      withCredentials: true, // Send authentication cookies
    });
  }

  // Update an existing transaction
  updateTransaction(transactionId: string, updateData: any): Observable<any> {
    return this.httpService.put(`transactions/${transactionId}`, updateData, {
      withCredentials: true,
    });
  }

  // Delete a transaction
  deleteTransaction(transactionId: string): Observable<any> {
    return this.httpService.delete( `transactions/${transactionId}`, {
      withCredentials: true,
    });
  }

  // Fetch transactions by category
  getTransactionsByCategory(category: string): Observable<any> {
    const params = new HttpParams().set('category', category); // Use HttpParams to set the query parameter
    return this.httpService.get('transactions/search', {
      params,
      withCredentials: true,
    });
  }
}
