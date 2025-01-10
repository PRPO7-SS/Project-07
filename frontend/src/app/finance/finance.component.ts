import { Component, OnInit } from '@angular/core';
import { NavigationComponent } from '../navigation/navigation.component';
import { TransactionService } from '../services/transaction.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-finance',
  standalone: true,
  imports: [NavigationComponent, CommonModule, FormsModule, FooterComponent],
  templateUrl: './finance.component.html',
  styleUrls: ['./finance.component.css'],
})
export class FinanceComponent implements OnInit {
  currentLang = 'en'; // Default language
  transactions: any[] = [];
  groupedTransactions: { date: string; transactions: any[] }[] = [];
  weeklySummary: { date: Date; spent: number; earned: number }[] = [];

  incomeCategories: string[] = ['Salary', 'Scholarship', 'Gifts', 'Other'];
  expenseCategories: string[] = [
    'Groceries',
    'Clothes',
    'School',
    'Transportation',
    'Gifts',
    'Subscription',
    'Eating',
    'Health',
    'Selfcare',
    'Other',
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

  tipOfTheDay: string = ''; // Tip of the day feature

  tips: string[] = [
    'Track your expenses daily to avoid overspending.',
    'Set a monthly savings goal and stick to it.',
    'Review your subscriptions to cut unnecessary costs.',
    'Plan your meals to save on groceries.',
    'Use public transport to save on transportation costs.',
    'Set aside 20% of your income for future investments.',
    'Create a budget and review it weekly.'
  ];

  constructor(
    private readonly transactionService: TransactionService,
    private readonly router: Router
  ) {
    console.log('FinanceComponent initialized.');
    this.weekRange = this.getWeekRange(this.currentDate);
  }

  ngOnInit(): void {
    console.log('Component initialized, calling updateWeekRange and loadTransactions.');
    this.updateWeekRange();
    this.loadTransactions();
    this.generateTipOfTheDay(); // Initialize Tip of the Day
  }

  generateTipOfTheDay(): void {
    console.log('Generating tip of the day...');
    const randomIndex = Math.floor(Math.random() * this.tips.length);
    this.tipOfTheDay = this.tips[randomIndex];
    console.log('Tip of the day:', this.tipOfTheDay);
  }

  loadTransactions(): void {
    console.log('Attempting to load transactions...');
    this.transactionService.getUserTransactions().subscribe({
      next: (data) => {
        console.log('Raw transactions data from backend:', data);

        this.transactions = (data || []).map((transaction: any) => ({
          ...transaction,
          date: transaction.date ? new Date(transaction.date) : null, // Parse ISO date to Date object
          createdAt: transaction.createdAt ? new Date(transaction.createdAt) : null,
          updatedAt: transaction.updatedAt ? new Date(transaction.updatedAt) : null,
        }));

        console.log('Parsed transactions:', this.transactions);

        this.groupedTransactions = this.groupTransactionsByDate(this.transactions);
        console.log('Grouped transactions by date:', this.groupedTransactions);

        this.calculateWeeklySummary();
        console.log('Weekly summary after calculation:', this.weeklySummary);
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
    console.log('Calculating weekly summary...');
    const start = this.weekRange.start;

    this.weeklySummary = Array.from({ length: 7 }, (_, i) => {
      const date = new Date(start);
      date.setDate(start.getDate() + i);
      return { date, spent: 0, earned: 0 };
    });

    console.log('Initial weekly summary structure:', this.weeklySummary);

    this.transactions.forEach((transaction) => {
      if (!transaction.date) return; // Skip transactions without a valid date
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

    console.log('Weekly summary after processing transactions:', this.weeklySummary);
  }

  isSameDay(date1: Date, date2: Date): boolean {
    return (
      date1.getFullYear() === date2.getFullYear() &&
      date1.getMonth() === date2.getMonth() &&
      date1.getDate() === date2.getDate()
    );
  }

  showDayTransactions(day: { date: Date; spent: number; earned: number }): void {
    console.log('Showing transactions for day:', day);
    const transactionsForDay = this.transactions.filter((transaction) =>
      this.isSameDay(new Date(transaction.date), day.date)
    );

    this.selectedDay = { date: day.date, transactions: transactionsForDay };
    console.log('Transactions for the selected day:', this.selectedDay);
  }

  showWeeklySummary(): void {
    console.log('Switching back to weekly summary view.');
    this.selectedDay = null;
  }

  submitTransaction(): void {
    console.log('Submitting transaction:', this.newTransaction);
    this.transactionService.addTransaction({
      ...this.newTransaction,
      date: new Date(this.newTransaction.date).toISOString(), // Save date in ISO format
    }).subscribe({
      next: () => {
        console.log('Transaction added successfully.');
        this.successMessage = 'Transaction added successfully!';
        this.errorMessage = '';
        this.addTransactionToWeeklySummary(this.newTransaction);
        this.addTransactionToGroupedTransactions(this.newTransaction);
        this.resetForm();
        this.loadTransactions();
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (err) => {
        console.error('Error adding transaction:', err);
        this.errorMessage = 'Error adding transaction. Please try again.';
        this.successMessage = '';
        setTimeout(() => (this.errorMessage = ''), 3000);
      },
    });
  }

  addTransactionToWeeklySummary(transaction: any): void {
    console.log('Adding transaction to weekly summary:', transaction);
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
    console.log('Weekly summary after adding transaction:', this.weeklySummary);
  }

  addTransactionToGroupedTransactions(transaction: any): void {
    console.log('Adding transaction to grouped transactions:', transaction);
    const transactionDateStr = new Date(transaction.date).toDateString();
    const existingGroup = this.groupedTransactions.find((group) => group.date === transactionDateStr);
    if (existingGroup) {
      existingGroup.transactions.push(transaction);
    } else {
      this.groupedTransactions.push({
        date: transactionDateStr,
        transactions: [transaction],
      });
    }
    console.log('Grouped transactions after addition:', this.groupedTransactions);
  }

  resetForm(): void {
    console.log('Resetting form...');
    this.newTransaction = {
      type: '',
      amount: 0,
      category: '',
      date: '',
    };
  }

  prevWeek(): void {
    console.log('Navigating to previous week...');
    this.currentDate.setDate(this.currentDate.getDate() - 7);
    this.updateWeekRange();
    this.loadTransactions();
  }

  nextWeek(): void {
    console.log('Navigating to next week...');
    this.currentDate.setDate(this.currentDate.getDate() + 7);
    this.updateWeekRange();
    this.loadTransactions();
  }

  updateWeekRange(): void {
    console.log('Updating week range...');
    this.weekRange = this.getWeekRange(this.currentDate);
    console.log('Updated week range:', this.weekRange);
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
    console.log('Grouping transactions by date...');
    const grouped = transactions.reduce((acc: any, transaction) => {
      const transactionDateStr = new Date(transaction.date).toDateString();
      if (!acc[transactionDateStr]) {
        acc[transactionDateStr] = [];
      }
      acc[transactionDateStr].push(transaction);
      return acc;
    }, {});

    const result = Object.keys(grouped).map((date) => ({
      date,
      transactions: grouped[date],
    }));

    console.log('Grouped transactions result:', result);
    return result;
  }

  deleteTransaction(transactionId: string): void {
    console.log('Attempting to delete transaction. Received ID:', transactionId);

    if (!transactionId) {
        console.error('Transaction ID is undefined or invalid');
        return;
    }

    this.transactionService.deleteTransaction(transactionId).subscribe({
        next: () => {
            console.log('Transaction deleted successfully:', transactionId);
            this.successMessage = 'Transaction deleted successfully!';
            setTimeout(() => (this.successMessage = ''), 3000);
            this.transactions = this.transactions.filter(transaction=>transaction.id!=transactionId);
            if (this.selectedDay) {
              this.selectedDay.transactions = this.selectedDay.transactions.filter(transaction=>
              transaction.id!=transactionId)
            }
            this.loadTransactions();
        },
        error: (err) => {
            this.errorMessage = 'Error deleting transaction. Please try again.';
            console.error('Error deleting transaction:', err);
            setTimeout(() => (this.errorMessage = ''), 3000);
        },
    });
  }
}