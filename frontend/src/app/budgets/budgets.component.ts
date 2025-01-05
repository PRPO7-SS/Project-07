import { Component, OnInit } from '@angular/core';
import { BudgetService } from '../services/budgets.service';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavigationComponent } from '../navigation/navigation.component';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-budgets',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NavigationComponent, FooterComponent],
  templateUrl: './budgets.component.html',
  styleUrls: ['./budgets.component.css'],
})
export class BudgetsComponent implements OnInit {
  budgets: any[] = [];
  groupedBudgets: { category: string; budgets: any[] }[] = [];
  budgetForm: FormGroup;
  selectedBudget: { category: string; monthlyLimit: number } | null = null;

  // Notification messages
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private readonly budgetService: BudgetService,
    private readonly fb: FormBuilder
  ) {
    this.budgetForm = this.fb.group({
      category: ['', [Validators.required]],
      monthlyLimit: ['', [Validators.required, Validators.min(1)]],
    });
  }

  ngOnInit(): void {
    this.loadBudgets();
  }

  // Fetch budgets
  loadBudgets(): void {
    this.budgetService.getBudgets().subscribe({
      next: (data) => {
        this.budgets = data || [];
        this.groupedBudgets = this.groupBudgetsByCategory(this.budgets);
        this.successMessage = 'Budgets loaded successfully!';
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (err) => {
        console.error('Error loading budgets:', err);
        this.errorMessage = 'Failed to load budgets.';
        setTimeout(() => (this.errorMessage = ''), 3000);
      },
    });
  }

  // Add a new budget
  submitBudget(): void {
    if (this.budgetForm.invalid) {
      this.errorMessage = 'Please fill in all required fields.';
      setTimeout(() => (this.errorMessage = ''), 3000);
      return;
    }

    const budgetData = this.budgetForm.value;
    this.budgetService.addBudget(budgetData).subscribe({
      next: () => {
        this.successMessage = 'Budget added successfully!';
        this.errorMessage = '';
        this.resetForm();
        this.loadBudgets();
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (err) => {
        console.error('Error adding budget:', err);
        this.errorMessage = 'Failed to add budget. Please try again.';
        setTimeout(() => (this.errorMessage = ''), 3000);
      },
    });
  }

  // Edit an existing budget
  editBudget(budget: any): void {
    this.selectedBudget = { category: budget.category, monthlyLimit: budget.monthlyLimit };
    this.budgetForm.patchValue(this.selectedBudget);
  }

  updateBudget(): void {
    if (this.selectedBudget === null || this.budgetForm.invalid) {
      this.errorMessage = 'Please select a budget and fill in all fields.';
      setTimeout(() => (this.errorMessage = ''), 3000);
      return;
    }
  
    const updatedLimit = this.budgetForm.value.monthlyLimit; // Extract only the new limit
    this.budgetService.updateBudget(this.selectedBudget.category, updatedLimit).subscribe({
      next: () => {
        console.log('Budget updated successfully.');
        this.successMessage = 'Budget updated successfully!';
        this.errorMessage = '';
        this.resetForm();
        this.selectedBudget = null;
        this.loadBudgets();
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (err) => {
        console.error('Error updating budget:', err);
        this.errorMessage = 'Error updating budget. Please try again.';
        this.successMessage = '';
        setTimeout(() => (this.errorMessage = ''), 3000);
      },
    });
  }

  // Delete a budget
  deleteBudget(category: string): void {
    if (!confirm(`Are you sure you want to delete the budget for "${category}"?`)) {
      return;
    }

    this.budgetService.deleteBudget(category).subscribe({
      next: () => {
        this.successMessage = 'Budget deleted successfully!';
        this.errorMessage = '';
        this.loadBudgets();
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (err) => {
        console.error('Error deleting budget:', err);
        this.errorMessage = 'Failed to delete budget. Please try again.';
        setTimeout(() => (this.errorMessage = ''), 3000);
      },
    });
  }

  // Group budgets by category
  groupBudgetsByCategory(budgets: any[]): { category: string; budgets: any[] }[] {
    const grouped = budgets.reduce((acc: any, budget: any) => {
      const category = budget.category;
      if (!acc[category]) {
        acc[category] = [];
      }
      acc[category].push(budget);
      return acc;
    }, {});

    return Object.keys(grouped).map((category) => ({
      category,
      budgets: grouped[category],
    }));
  }

  // Reset the form
  resetForm(): void {
    this.budgetForm.reset();
    this.selectedBudget = null;
  }
}