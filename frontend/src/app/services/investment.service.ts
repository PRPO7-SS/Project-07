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
        return this.httpService.post<Investment>(this.endpoint, payload, { withCredentials: true });
    }

    /**
   * Fetch all investments
   */
    getInvestments(): Observable<Investment[]> {
        return this.httpService.get<Investment[]>(this.endpoint, { withCredentials: true });
    }


   /**
   * Delete an investment
   * @param id - Investment ID
   */
  deleteInvestment(id: string): Observable<any> {
    return this.httpService.delete<any>(`investments/${id}`, { withCredentials: true });
  }

}
