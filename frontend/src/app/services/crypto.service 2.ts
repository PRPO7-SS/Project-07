import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CryptoService {
  private readonly baseUrl = 'https://api.coingecko.com/api/v3';

  constructor(private readonly http: HttpClient) {}

  // Fetch the list of cryptocurrencies
  getCryptoList(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/coins/list`);
  }

  // Fetch supported currencies
  getSupportedCurrencies(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/simple/supported_vs_currencies`);
  }

  // Fetch cryptocurrency data
  getCryptoData(coinId: string, currency: string, days: number): Observable<any> {
    return this.http.get<any>(
      `${this.baseUrl}/coins/${coinId}/market_chart?vs_currency=${currency}&days=${days}`
    );
  }
}
