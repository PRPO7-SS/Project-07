// src/app/components/investments/investments.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NavigationComponent } from '../navigation/navigation.component';
import { InvestmentsService } from '../services/investment.service';
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
    quantity: 0,
    purchaseDate: new Date(),
  };

  missingFields: { [key: string]: boolean } = {};
  message: string | null = null;
  loading: boolean = false;
  selectedInvestment: any = null;

  investments: any[] = [];
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

  constructor(
    private readonly investmentService: InvestmentsService
  ) {}

  // protected modalRef?: BsModalRef;

  ngOnInit(): void {
    this.fetchInvestments();
  }

  fetchInvestments(): void {
    this.investmentService.getInvestments().subscribe((data) => {
      this.investments = data;
      console.log('Fetched investments:', this.investments);
    });
  }

  calculateFluctuation(investment: any): string {
    if (investment.currentValue) {
      const percentageChange = ((investment.currentValue - investment.amount) / investment.amount) * 100;
      return percentageChange.toFixed(2);
    }
    return 'N/A';
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

  editInvestment(investment: any): void {
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
    this.investmentService.updateInvestment(investment._id, this.formData).subscribe({
      next: (response: any) => {
        console.log('Investment updated successfully:', response);
        this.investments = this.investments.map((i) =>
          i._id === investment._id ? { ...i, ...this.formData } : i
        );
      },
      error: (error) => {
        console.error('Error updating investment:', error);
        alert('Error updating investment. Please try again.');
      },
    });
  }

  protected closeModal() {
    // this.modalRef?.hide();
  }
}
