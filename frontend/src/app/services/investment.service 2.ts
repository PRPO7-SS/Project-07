// src/app/core/services/investments.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Investment, CreateInvestmentRequest, UpdateInvestmentRequest } from '../models/investment';
import { HttpService } from './http.service';


@Injectable({
  providedIn: 'root',
})
export class InvestmentsService {
    private readonly endpoint = 'investments'
    constructor(private readonly httpService: HttpService) {}

  // Create a new investment
    createInvestment(payload: CreateInvestmentRequest): Observable<Investment> {
        return this.httpService.post<Investment>('investmentSer', this.endpoint, payload, { withCredentials: true });
    }

    /**
   * Fetch all investments
   */
    getInvestments(): Observable<Investment[]> {
        return this.httpService.get<Investment[]>('investmentSer', this.endpoint, { withCredentials: true });
    }

   /**
   * Fetch a single investment by ID
   * @param id - Investment ID
   */
    getInvestmentById(id: string): Observable<Investment> {
        return this.httpService.get<Investment>('investmentSer', `investments/${id}`, { withCredentials: true });
    }

 /**
   * Update an existing investment
   * @param id - Investment ID
   * @param payload - Updated investment data
   */
    updateInvestment(id: string, payload: UpdateInvestmentRequest): Observable<Investment> {
        return this.httpService.put<Investment>('investmentSer', `investments/${id}`, payload, { withCredentials: true });
    }


   /**
   * Delete an investment
   * @param id - Investment ID
   */
  deleteInvestment(id: string): Observable<any> {
    return this.httpService.delete<any>('investmentSer', `investments/${id}`, { withCredentials: true });
  }

}
