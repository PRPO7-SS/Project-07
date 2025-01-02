import { Component, OnInit } from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { FinancialDataService } from '../../services/financial-data.service';
import { CryptoService } from '../../services/crypto.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DataCacheService } from '../../services/data-cache.service';

@Component({
  selector: 'app-crypto-chart',
  templateUrl: './crypto-chart.component.html',
  styleUrls: ['./crypto-chart.component.css'],
  imports: [CommonModule, FormsModule],
  standalone: true
})
export class CryptoChartComponent implements OnInit {
  chart: any;
  dataType: string = 'crypto'; // 'crypto' or 'stock'
  itemList: any[] = [];
  currencyList: string[] = ['usd', 'eur']; // Example currencies
  selectedCurrency: string = 'usd'; // Default currency
  selectedItem: string = 'bitcoin';
  timeframes = [
    { label: 'Day', value: 1 },
    { label: 'Week', value: 7 },
    { label: 'Month', value: 30 },
    { label: 'Year', value: 365 },
  ];
  selectedTimeframe: number = 0;

  allowedCryptos = [
    'bitcoin',
    'ethereum',
    'xrp',
    'solana',
    'chainlink',
    'bnb',
    'tether',
    'cardano',
  ];

  constructor(
    private readonly stockService: FinancialDataService,
    private readonly cryptoService: CryptoService,
    private readonly dataCacheService: DataCacheService
  ) {
    Chart.register(...registerables);
  }

  ngOnInit(): void {
    this.loadItems();
  }

  loadItems(): void {
    if (this.dataType === 'stock') {
      this.stockService.getStockList().subscribe(
        (data: any[]) => {
          this.selectedItem = 'NVDA';
          this.itemList = data; // Use the filtered stock list
        },
        (error) => console.error('Error loading stocks:', error)
      );
    } else if (this.dataType === 'crypto') {
      this.cryptoService.getCryptoList().subscribe((list: any[]) => {
        // Filter the crypto list to include only the allowed options
        this.itemList = list.filter((crypto) =>
          this.allowedCryptos.includes(crypto.id)
        );
      });
      this.selectedItem = 'bitcoin';
    }
  }

  onDataTypeChange(event: any): void {
    this.dataType = event.target.value;
    this.selectedItem = '';
    this.loadItems();
  }

  onItemChange(event: any): void {
    this.selectedItem = event.target.value;
    this.loadGraph();
  }

  onCurrencyChange(event: any): void {
    this.selectedCurrency = event.target.value;
    this.loadGraph();
  }

  onTimeframeButtonClick(value: number): void {
    this.selectedTimeframe = value;
    this.loadGraph();
  }

  loadGraph(): void {
    if (!this.selectedItem) return;

    const cacheKey = `${this.dataType}-${this.selectedItem}-${this.selectedCurrency}-${this.selectedTimeframe}`;
    const cachedData = this.dataCacheService.getCache(cacheKey);

    if (cachedData) {
      this.processGraphData(cachedData);
    } else {
      if (this.dataType === 'stock') {
        this.stockService
          .getStockData(this.selectedItem, this.selectedCurrency, '1h', this.selectedTimeframe)
          .subscribe((data: any) => {
            this.dataCacheService.setCache(cacheKey, data);
            this.processGraphData(data);
          });
      } else if (this.dataType === 'crypto') {
        this.cryptoService
          .getCryptoData(this.selectedItem, this.selectedCurrency, this.selectedTimeframe)
          .subscribe((data: any) => {
            this.dataCacheService.setCache(cacheKey, data);
            this.processGraphData(data);
          });
      }
    }
  }

  processGraphData(data: any): void {
    let labels, prices;
    if (this.dataType === 'stock') {
      labels = data.values.map((val: any) =>
        new Date(val.datetime).toLocaleDateString()
      );
      prices = data.values.map((val: any) => parseFloat(val.close));
      if (this.chart) this.chart.destroy();
      this.createStockChart(labels, prices, this.selectedCurrency.toUpperCase());
    } else if (this.dataType === 'crypto') {
      labels = data.prices.map((price: any) =>
        new Date(price[0]).toLocaleDateString()
      );
      prices = data.prices.map((price: any) => price[1]);
      if (this.chart) this.chart.destroy();
      this.createChart(labels, prices, this.selectedCurrency.toUpperCase());
    }
  }

  createChart(labels: string[], data: number[], currency: string): void {
    this.chart = new Chart('stockCryptoChart', {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          {
            label: `${this.selectedItem.toUpperCase()} Price (${currency})`,
            data: data,
            borderColor: 'green',
            borderWidth: 1,
            pointRadius: 1,
            tension: 0.3,
            fill: false,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            display: true,
            position: 'top',
          },
        },
        scales: {
          x: {
            display: true,
          },
          y: {
            display: true,
          },
        },
      },
    });
  }

  createStockChart(labels: string[], data: number[], currency: string): void {
    this.chart = new Chart('stockCryptoChart', {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          {
            label: `${this.selectedItem.toUpperCase()} Price (${currency})`,
            data: data,
            borderColor: 'green',
            borderWidth: 1,
            pointRadius: 1,
            tension: 0.3,
            fill: false,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            display: true,
            position: 'top',
          },
        },
        scales: {
          x: {
            reverse: true,
            display: true,
          },
          y: {
            display: true,
          },
        },
      },
    });
  }
}
