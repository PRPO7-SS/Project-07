import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class FinancialDataService {
  private readonly apiKey = environment.twelveDataApiKey; // Twelve Data API key
  private readonly baseUrl = environment.twelveDataUrl; // Twelve Data API base URL

  // Predefined list of allowed stocks
  private readonly allowedStocks = [
    'NVIDIA',
    'Tesla',
    'Apple',
    'Volkswagen',
    'Microsoft',
    'Amazon',
    'Disney',
    'Allianz',
    'Alphabet',
    'SAP',
    'Rheinmetall',
    'Bayer',
    'Siemens',
  ];

  constructor(private readonly http: HttpClient) {}

  /**
   * Fetches the current price of a stock or cryptocurrency.
   * @param symbol The stock or cryptocurrency symbol (e.g., BTC/USD or AAPL).
   * @returns An observable with the price data.
   */
  getPrice(symbol: string): Observable<any> {
    const url = `${this.baseUrl}/price?symbol=${symbol}&apikey=${this.apiKey}`;
    return this.http.get(url);
  }

  /**
   * Fetches historical prices for a stock or cryptocurrency.
   * @param symbol The stock or cryptocurrency symbol (e.g., BTC/USD or AAPL).
   * @param startDate The start date for historical data (YYYY-MM-DD format).
   * @param endDate The end date for historical data (YYYY-MM-DD format).
   * @param interval The interval between data points (e.g., 1day, 1hour).
   * @returns An observable with the historical price data.
   */
  getHistoricalPrices(
    symbol: string,
    startDate: string,
    endDate: string,
    interval: string = '1day'
  ): Observable<any> {
    const url = `${this.baseUrl}/time_series?symbol=${symbol}&interval=${interval}&start_date=${startDate}&end_date=${endDate}&apikey=${this.apiKey}`;
    return this.http.get(url);
  }

  /**
   * Fetches stock list and filters it to include only allowed stocks.
   * @returns An observable with the filtered list of allowed stocks.
   */
  getStockList(): Observable<any[]> {
    return new Observable((observer) => {
      // Mock data for stock list (Replace with API call if needed)
      const stocks = [
        { symbol: 'NVDA', name: 'NVIDIA' },
        { symbol: 'TSLA', name: 'Tesla' },
        { symbol: 'AAPL', name: 'Apple' },
        { symbol: 'VWAGY', name: 'Volkswagen' },
        { symbol: 'MSFT', name: 'Microsoft' },
        { symbol: 'AMZN', name: 'Amazon' },
        { symbol: 'DIS', name: 'Disney' },
        { symbol: 'ALV', name: 'Allianz' },
        { symbol: 'GOOGL', name: 'Alphabet' },
        { symbol: 'SAP', name: 'SAP' },
        { symbol: 'RHM', name: 'Rheinmetall' },
        { symbol: 'BAYRY', name: 'Bayer' },
        { symbol: 'SIEGY', name: 'Siemens' },
      ];

      // Filter stocks based on the allowed list
      const filteredStocks = stocks.filter((stock) =>
        this.allowedStocks.includes(stock.name)
      );

      observer.next(filteredStocks);
      observer.complete();
    });
  }

  /**
   * Fetches stock data for a given symbol, currency, interval, and time range.
   * @param symbol The stock symbol (e.g., AAPL).
   * @param currency The currency (e.g., USD, EUR).
   * @param interval The data interval (e.g., 1day, 1hour).
   * @param days The number of days for which to fetch data.
   * @returns An observable with the stock data.
   */
  getStockData(
    symbol: string,
    currency: string,
    interval: string,
    days: number
  ): Observable<any> {
    const today = new Date();
    const startDate = new Date(today);
    startDate.setDate(today.getDate() - days); // Calculate start date

    const startDateString = startDate.toISOString().split('T')[0]; // Format: YYYY-MM-DD
    const url = `${this.baseUrl}/time_series?symbol=${symbol}&interval=${interval}&start_date=${startDateString}&apikey=${this.apiKey}`;

    return this.http.get(url);
  }
}
