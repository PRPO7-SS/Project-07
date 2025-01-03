// src/app/components/investments/investments.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NavigationComponent } from '../navigation/navigation.component';
import { InvestmentsService } from '../services/investment.service';
import { FinancialDataService } from '../services/financial-data.service';
import { InvestmentType } from '../models/investment';
import { CryptoChartComponent } from '../homepage/crypto-chart/crypto-chart.component';
// import { ModalModule } from 'ngx-bootstrap/modal';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

@Component({
  selector: 'app-investments',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavigationComponent, CryptoChartComponent,
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

  constructor(
    private readonly investmentService: InvestmentsService,
    private readonly finDataService: FinancialDataService,
  ) {}

  // protected modalRef?: BsModalRef;

  ngOnInit(): void {
    this.fetchInvestments();
    this.loadItems();
  }

  fetchInvestments(): void {
    this.investmentService.getInvestments().subscribe((data) => {
      this.investments = data;
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

  onDataTypeChange(event: any, edit: boolean): void {
    this.dataType = event.target.value;
    if(!edit)
      this.selectedItem = '';
    else
      this.selectedItem = this.selectedInvestment.name;
    this.loadItems();
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

    this.calculatedAllocation.stocks = (this.allocation.stocks / 100) * this.availableCapital;
    this.calculatedAllocation.crypto = (this.allocation.crypto / 100) * this.availableCapital;
    this.calculatedAllocation.savings = (this.allocation.savings / 100) * this.availableCapital;

    console.log('Calculated allocations:', this.calculatedAllocation);
  }

  onAddInvestment(event: Event) {
    event.preventDefault();
    this.missingFields = {};

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
      this.message = 'Please fill in all required fields.';
      return;
    }

    this.closeModal();

    this.loading = true;
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

  protected closeModal() {
    // this.modalRef?.hide();
  }
}
