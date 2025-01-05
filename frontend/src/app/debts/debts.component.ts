import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavigationComponent } from '../navigation/navigation.component';
import { FooterComponent } from '../footer/footer.component';
import { DebtService } from '../services/debt.service';

@Component({
  selector: 'app-debts',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NavigationComponent, FooterComponent],
  templateUrl: './debts.component.html',
  styleUrls: ['./debts.component.css'],
})
export class DebtsComponent implements OnInit {
  debts: any[] = [];
  debtForm: FormGroup;
  selectedDebt: any = null;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(private debtService: DebtService, private fb: FormBuilder) {
    this.debtForm = this.fb.group({
      creditor: ['', [Validators.required]],
      description: ['', [Validators.required]],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      isPaid: [false],
      deadline: ['', [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.loadDebts();
  }

  loadDebts(): void {
    this.debtService.getDebts().subscribe({
      next: (data) => (this.debts = data),
      error: (err) => (this.errorMessage = 'Failed to load debts'),
    });
  }

  submitDebt(): void {
    if (this.debtForm.invalid) {
      this.errorMessage = 'Please fill in all required fields.';
      setTimeout(() => (this.errorMessage = ''), 3000);
      return;
    }
  
    if (this.selectedDebt && this.selectedDebt.id) {
      // Posodobimo obstoječi dolg
      const updatedDebtData = {
        ...this.debtForm.value,
        deadline: new Date(this.debtForm.value.deadline).toISOString(), // Pretvori datum v ISO format
      };
  
      this.debtService.updateDebt(this.selectedDebt.id, updatedDebtData).subscribe({
        next: () => {
          this.successMessage = 'Debt updated successfully!';
          this.errorMessage = '';
          this.loadDebts(); // Osveži seznam dolgov
          this.resetForm(); // Ponastavi obrazec
          this.selectedDebt = null; // Ponastavi `selectedDebt`
          setTimeout(() => (this.successMessage = ''), 3000);
        },
        error: (err) => {
          console.error('Error updating debt:', err);
          this.errorMessage = 'Failed to update debt. Please try again.';
          setTimeout(() => (this.errorMessage = ''), 3000);
        },
      });
    } else {
      // Dodajamo nov dolg
      const debtData = {
        ...this.debtForm.value,
        deadline: new Date(this.debtForm.value.deadline).toISOString(), // Pretvori datum v ISO format
      };
  
      this.debtService.addDebt(debtData).subscribe({
        next: () => {
          this.successMessage = 'Debt added successfully!';
          this.errorMessage = '';
          this.loadDebts(); // Osveži seznam dolgov
          this.resetForm(); // Ponastavi obrazec
          setTimeout(() => (this.successMessage = ''), 3000);
        },
        error: (err) => {
          console.error('Error adding debt:', err);
          this.errorMessage = 'Failed to add debt. Please try again.';
          setTimeout(() => (this.errorMessage = ''), 3000);
        },
      });
    }
  }

  editDebt(debt: any): void {
    console.log('Selected Debt:', debt); // Debugging
    this.selectedDebt = debt;
    this.debtForm.patchValue({
      creditor: debt.creditor,
      description: debt.description,
      amount: debt.amount,
      deadline: new Date(debt.deadline).toISOString().substring(0, 10),
    });
  }

  updateDebt(): void {
    if (!this.selectedDebt || !this.selectedDebt.id) {
      console.error('No debt selected for updating.');
      return;
    }
  
    const updatedDebtData = {
      ...this.debtForm.value,
      deadline: new Date(this.debtForm.value.deadline).toISOString(),
    };
  
    console.log('Updating debt with ID:', this.selectedDebt.id);
    console.log('Updated debt data:', updatedDebtData);
  
    this.debtService.updateDebt(this.selectedDebt.id, updatedDebtData).subscribe({
      next: () => {
        console.log('Debt updated successfully.');
        this.successMessage = 'Debt updated successfully!';
        this.loadDebts();
        this.resetForm();
        this.selectedDebt = null;
      },
      error: (err) => {
        console.error('Error updating debt:', err);
        this.errorMessage = 'Failed to update debt.';
      },
    });
  }

  deleteDebt(debtId: string): void {
    this.debtService.deleteDebt(debtId).subscribe({
      next: () => {
        this.successMessage = 'Debt marked as paid successfully!';
        this.errorMessage = '';
        this.loadDebts(); // Osveži seznam
        this.debtForm.reset();
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: () => (this.errorMessage = 'Failed to delete debt'),
    });
  }

  markAsPaid(debtId: string): void {
    this.debtService.markDebtAsPaid(debtId).subscribe({
      next: () => {
        this.successMessage = 'Debt marked as paid successfully!';
        this.loadDebts();
      },
      error: () => (this.errorMessage = 'Failed to mark debt as paid'),
    });
  }

  resetForm(): void {
    this.selectedDebt = null;
    this.debtForm.reset();
  }
}