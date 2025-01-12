// src/app/components/investments/investments.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NavigationComponent } from '../navigation/navigation.component';
import { InvestmentsService } from '../services/investment.service';
import { FinancialDataService } from '../services/financial-data.service';
import { TransactionService } from '../services/transaction.service';
import { InvestmentType } from '../models/investment';
import { CryptoChartComponent } from '../homepage/crypto-chart/crypto-chart.component';
// import { ModalModule } from 'ngx-bootstrap/modal';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-investments',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavigationComponent, CryptoChartComponent, FooterComponent
     ],
  templateUrl: './investments.component.html',
  styleUrls: ['./investments.component.css'],
})
export class InvestmentsComponent implements OnInit {
  formData = {
    type: '' as InvestmentType,
    name: '',
    amount: 0,
    quantity: 0.0,
    purchaseDate: new Date(),
  };

  missingFields: { [key: string]: boolean } = {};
  message: string | null = null;
  loading: boolean = false;
  selectedInvestment: any = null;

  investments: any[] = [];
  itemList: any[] = [];
  allocation = {
    stocks: 0,
    crypto: 0,
    savings: 0,
  };
  calculatedAllocation = {
    stocks: 0,
    crypto: 0,
    savings: 0,
  };
  availableCapital: number = 0;
  dataType: string = 'crypto';
  selectedItem: string = 'bitcoin';
  totalInvestments: number = 0;
  totalStocks: number = 0;
  totalCrypto: number = 0;
  currentTotalInvestments: number = 0;
  currentTotalStocks: number = 0;
  currentTotalCrypto: number = 0;


  constructor(
    private readonly investmentService: InvestmentsService,
    private readonly finDataService: FinancialDataService,
    private readonly transactionService: TransactionService,
  ) {}

  // protected modalRef?: BsModalRef;

  ngOnInit(): void {
    this.fetchInvestments();
    this.loadItems();
    this.calculateAvailableCapital();
  }

  fetchInvestments(): void {
    this.investmentService.getInvestments().subscribe((data) => {
      this.investments = data;
      this.calculateTotalInvestments();
      this.calculateTotalStocks();
      this.calculateTotalCrypto();
      this.updateCurrentValues();
      console.log('Fetched investments:', this.investments);
    });
  }

  loadItems(): void {
    if (this.dataType === 'stock') {
      this.finDataService.getStockList().subscribe(
        (data: any[]) => {
          this.selectedItem = 'NVDA';
          this.itemList = data; // Use the filtered stock list
        },
        (error) => console.error('Error loading stocks:', error)
      );
    } else if (this.dataType === 'crypto') {
      this.finDataService.getCryptoList().subscribe(
        (data: any[]) => {
          this.selectedItem = 'BTC';
          this.itemList = data; // Use the filtered stock list
        },
        (error) => console.error('Error loading stocks:', error)
      );
    }
  }

  onDataTypeChange(event: any): void {
    this.dataType = event.target.value;
    this.selectedItem = '';
    this.loadItems();
  }

  calculateAvailableCapital(): void {
    const currentMonth = new Date().getMonth();
    const currentYear = new Date().getFullYear();

    this.transactionService.getUserTransactions().subscribe({
      next: (data: any) => {
        const transactions = data;

        const income = transactions
          .filter((transaction: any) => {
            const date = new Date(transaction.date);
            return (
              transaction.type === 'Income' &&
              date.getMonth() === currentMonth &&
              date.getFullYear() === currentYear
            );
          })
          .reduce((sum: number, transaction: any) => sum + transaction.amount, 0);

        const expenses = transactions
          .filter((transaction: any) => {
            const date = new Date(transaction.date);
            return (
              transaction.type === 'Expense' &&
              date.getMonth() === currentMonth &&
              date.getFullYear() === currentYear
            );
          })
          .reduce((sum: number, transaction: any) => sum + transaction.amount, 0);

        this.availableCapital = income - expenses;
      },
      error: (error) => {
        console.error('Error fetching transactions:', error);
      },
    });
  }

  calculateTotalInvestments(): void {
    this.totalInvestments = this.investments.reduce((sum, investment) => sum + investment.amount, 0);
  }

  calculateTotalStocks(): void {
    this.totalStocks = this.investments
      .filter((investment) => investment.type.toLowerCase() === 'stock')
      .reduce((sum, stock) => sum + stock.amount, 0);
  }

  calculateTotalCrypto(): void {
    this.totalCrypto = this.investments
      .filter((investment) => investment.type.toLowerCase() === 'crypto')
      .reduce((sum, crypto) => sum + crypto.amount, 0);
  }


  calculateFluctuation(investment: any): string {
      const percentageChange = ((investment.currentValue - investment.amount) / investment.amount) * 100;
      return percentageChange.toFixed(2);
  }

  calculateAllocations(): void {
    const totalPercentage = this.allocation.stocks + this.allocation.crypto + this.allocation.savings;

    if (totalPercentage > 100) {
      this.message = 'Total allocation percentage cannot exceed 100%.';
      return;
    }

    this.calculatedAllocation.stocks = ((this.allocation.stocks / 100) * this.availableCapital);
    this.calculatedAllocation.crypto = (this.allocation.crypto / 100) * this.availableCapital;
    this.calculatedAllocation.savings = (this.allocation.savings / 100) * this.availableCapital;

    console.log('Calculated allocations:', this.calculatedAllocation);
  }

  onAddInvestment(event: Event) {
    event.preventDefault();
    this.missingFields = {};
    console.log('klicem');

    if (!this.formData.type.trim()) {
      this.missingFields['type'] = true;
    }
    if (!this.formData.name.trim()) {
      this.missingFields['name'] = true;
    }
    if (this.formData.amount <= 0) {
      this.missingFields['amount'] = true;
    }
    if (this.formData.quantity <= 0) {
      this.missingFields['quantity'] = true;
    }
    if (!this.formData.purchaseDate) {
      this.missingFields['purchaseDate'] = true;
    }

    if (Object.keys(this.missingFields).length > 0) {
      console.log('klicem2');
      this.message = 'Please fill in all required fields.';
      return;
    }

    this.closeModal();

    this.loading = true;
    console.log('klicem');
    this.investmentService.createInvestment(this.formData).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.message = 'Investment successfully added!';
        this.fetchInvestments();
      },
      error: (error: { error: { message: string }; status: number }) => {
        this.loading = false;
        this.message = error.error?.message || 'Error adding investment.';
      },
    });
  }

  deleteInvestment(investment: any): void {
    this.investmentService.deleteInvestment(investment.id).subscribe({
      next: () => {
        console.log('se izvede');
        this.investments = this.investments.filter((i) => i._id !== investment._id);
        this.fetchInvestments();
      },
      error: (error) => {
        console.error('Error deleting investment:', error);
      },
    });
  }

  updateCurrentValues(): void {
        this.currentTotalInvestments = 0;
        this.currentTotalStocks = 0;
        this.currentTotalCrypto = 0;

        this.investments.forEach((investment) => {

          this.currentTotalInvestments += investment.currentValue;

          // Dodamo v ustrezno kategorijo
          if (investment.type.toLowerCase() === 'stock') {
            this.currentTotalStocks += investment.currentValue;
          } else if (investment.type.toLowerCase() === 'crypto') {
            this.currentTotalCrypto += investment.currentValue;
          }
        });
  }

  protected closeModal() {
    // this.modalRef?.hide();
  }
}
