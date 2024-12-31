import { Component, OnInit } from '@angular/core';
import { NavigationComponent } from '../navigation/navigation.component';
import { TransactionService } from '../services/transaction.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-finance',
  standalone: true,
  imports: [NavigationComponent, CommonModule, FormsModule],
  templateUrl: './finance.component.html',
  styleUrls: ['./finance.component.css'],
})
export class FinanceComponent implements OnInit {
  currentLang = 'en'; // Default language
  transactions: any[] = [];
  groupedTransactions: { date: string; transactions: any[] }[] = [];
  weeklySummary: { date: Date; spent: number; earned: number }[] = [];

  incomeCategories: string[] = ['salary', 'scholarship', 'gifts', 'other'];
  expenseCategories: string[] = [
    'groceries',
    'clothes',
    'school',
    'transportation',
    'gifts',
    'subscription',
    'eating',
    'health',
    'selfcare',
    'other',
  ];
  newTransaction = {
    type: '',
    amount: 0,
    category: '',
    date: '',
  };

  currentDate = new Date();
  weekRange: { start: Date; end: Date };

  successMessage: string = '';
  errorMessage: string = '';

  selectedDay: { date: Date; transactions: any[] } | null = null;

  constructor(
    private readonly transactionService: TransactionService,
    private readonly router: Router
  ) {
    this.weekRange = this.getWeekRange(this.currentDate);
  }

  ngOnInit(): void {
    this.updateWeekRange();
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.transactionService.getUserTransactions().subscribe({
      next: (data) => {
        this.transactions = data.transactions || [];
        this.groupedTransactions = this.groupTransactionsByDate(this.transactions);
        this.calculateWeeklySummary();
      },
      error: (err) => {
        console.error('Error fetching transactions:', err);
        this.errorMessage = 'Failed to load transactions. Please try again.';
        setTimeout(() => (this.errorMessage = ''), 3000);
        this.transactions = [];
        this.calculateWeeklySummary();
      },
    });
  }

  calculateWeeklySummary(): void {
    const start = this.weekRange.start;

    this.weeklySummary = Array.from({ length: 7 }, (_, i) => {
      const date = new Date(start);
      date.setDate(start.getDate() + i);
      return { date, spent: 0, earned: 0 };
    });

    this.transactions.forEach((transaction) => {
      const transactionDate = new Date(transaction.date);
      this.weeklySummary.forEach((day) => {
        if (this.isSameDay(day.date, transactionDate)) {
          if (transaction.type.toLowerCase() === 'expense') {
            day.spent += transaction.amount;
          } else if (transaction.type.toLowerCase() === 'income') {
            day.earned += transaction.amount;
          }
        }
      });
    });
  }

  isSameDay(date1: Date, date2: Date): boolean {
    return (
      date1.getFullYear() === date2.getFullYear() &&
      date1.getMonth() === date2.getMonth() &&
      date1.getDate() === date2.getDate()
    );
  }

  showDayTransactions(day: { date: Date; spent: number; earned: number }): void {
    const transactionsForDay = this.transactions.filter((transaction) =>
      this.isSameDay(new Date(transaction.date), day.date)
    );

    this.selectedDay = { date: day.date, transactions: transactionsForDay };
  }

  showWeeklySummary(): void {
    this.selectedDay = null;
  }

  submitTransaction(): void {
    this.transactionService.addTransaction(this.newTransaction).subscribe({
      next: () => {
        this.successMessage = 'Transaction added successfully!';
        this.errorMessage = '';
        this.resetForm();
        this.loadTransactions();
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (err) => {
        this.errorMessage = 'Error adding transaction. Please try again.';
        this.successMessage = '';
        console.error('Error adding transaction:', err);
        setTimeout(() => (this.errorMessage = ''), 3000);
      },
    });
  }

  resetForm(): void {
    this.newTransaction = {
      type: '',
      amount: 0,
      category: '',
      date: '',
    };
  }

  prevWeek(): void {
    this.currentDate.setDate(this.currentDate.getDate() - 7);
    this.updateWeekRange();
    this.loadTransactions();
  }

  nextWeek(): void {
    this.currentDate.setDate(this.currentDate.getDate() + 7);
    this.updateWeekRange();
    this.loadTransactions();
  }

  updateWeekRange(): void {
    this.weekRange = this.getWeekRange(this.currentDate);
  }

  formatDate(date: Date): string {
    const options: Intl.DateTimeFormatOptions = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Intl.DateTimeFormat(this.currentLang, options).format(date);
  }

  getWeekRange(date: Date): { start: Date; end: Date } {
    const currentDate = new Date(date);
    const dayOfWeek = currentDate.getDay();
    const diffToMonday = (dayOfWeek === 0 ? -6 : 1) - dayOfWeek;

    const startOfWeek = new Date(currentDate);
    startOfWeek.setDate(currentDate.getDate() + diffToMonday);

    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 6);

    return { start: startOfWeek, end: endOfWeek };
  }

  groupTransactionsByDate(transactions: any[]): { date: string; transactions: any[] }[] {
    const grouped = transactions.reduce((acc: any, transaction) => {
      const transactionDateStr = new Date(transaction.date).toDateString();
      if (!acc[transactionDateStr]) {
        acc[transactionDateStr] = [];
      }
      acc[transactionDateStr].push(transaction);
      return acc;
    }, {});

    return Object.keys(grouped).map((date) => ({
      date,
      transactions: grouped[date],
    }));
  }

  deleteTransaction(transactionId: string): void {
    this.transactionService.deleteTransaction(transactionId).subscribe({
      next: () => {
        this.loadTransactions();
        this.successMessage = 'Transaction deleted successfully!';
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (err) => {
        this.errorMessage = 'Error deleting transaction. Please try again.';
        console.error('Error deleting transaction:', err);
        setTimeout(() => (this.errorMessage = ''), 3000);
      },
    });
  }
}