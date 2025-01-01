import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationComponent } from '../navigation/navigation.component';
import { RouterModule } from '@angular/router';
import { CalendarOptions } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { FullCalendarModule } from '@fullcalendar/angular';
//import { TransactionService } from '../services/transaction.service';
import { SavingsGoalService } from '../services/savings-goal.service';
import { Transaction } from '../models/transaction';
import { SavingsGoal } from '../models/savingsGoal';
import { CryptoChartComponent } from './crypto-chart/crypto-chart.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-homepage',
  standalone: true,
  imports: [NavigationComponent, CommonModule, RouterModule, FullCalendarModule, CryptoChartComponent, FormsModule],
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css'],
})
export class HomepageComponent implements OnInit {
  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, interactionPlugin],
    initialView: 'dayGridMonth',
    locale: 'en',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: ''
    },
    buttonText: {
      today: ''
    },
    firstDay: 1,
    expandRows: true,
    events: [],
    dateClick: this.handleDateClick.bind(this),
    selectable: true
  };

  selectedTransactions: any[] = [];
  selectedGoals: any[] = [];
  successMessage: string = '';
  errorMessage: string | null = null;
  availableCapital: number = 10000;

  newGoal = {
    goalName: '',
    targetAmount: 0,
    currentAmount: 0,
    startDate: '',
    deadline: '',
  };

  editGoal: any = {};

  isEditMode = false;
  goalId = '';

  constructor(
    private readonly savingsGoalService: SavingsGoalService
  ) {}

  ngOnInit(): void {
    this.loadToCalendar();
    this.calculateAvailableCapital();
  }

  loadToCalendar(): void {
    const transactionData = { transactions: [] }; // Placeholder for transactions as []
    const aggregatedTransactions = this.aggregateTransactions(transactionData.transactions);
    const transactionEvents = aggregatedTransactions.map((entry) => ({
      title: `${entry.type}: ${entry.amount}€`,
      start: entry.date,
      backgroundColor: entry.type === 'Income' ? '#28a745' : '#dc3545',
      borderColor: entry.type === 'Income' ? '#28a745' : '#dc3545',
      textColor: '#fff',
      allDay: true,
    }));

    this.savingsGoalService.getSavingsGoals().subscribe({
      next: (savingsGoalData: any[]) => {
        const savingsGoalEvents = savingsGoalData.map((goal) => ({
          title: goal.goalName,
          start: goal.deadline,
          backgroundColor: '#fff',
          borderColor: '#000',
          textColor: '#000',
          allDay: true,
        }));
        const events = [...transactionEvents, ...savingsGoalEvents];
        this.calendarOptions.events = events;
      },
      error: (err) => {
        if (err.status === 404) {
          this.calendarOptions.events = transactionEvents;
        } else {
          console.error('Error loading savings goals', err);
        }
      }
    });
  }

  private aggregateTransactions(transactions: Transaction[]): { date: string; type: 'Income' | 'Expense'; amount: number }[] {
    const map = new Map<string, { date: string; type: 'Income' | 'Expense'; amount: number }>();

    transactions.forEach((transaction) => {
      const key = `${transaction.date}-${transaction.type}`;
      if (!map.has(key)) {
        map.set(key, { date: transaction.date, type: transaction.type, amount: 0 });
      }
      map.get(key)!.amount += transaction.amount;
    });

    return Array.from(map.values());
  }

  handleDateClick(info: any): void {
    const clickedDate = info.dateStr;

    this.selectedTransactions = []; // Placeholder for empty transactions

    this.savingsGoalService.getSavingsGoals().subscribe({
      next: (data) => {
        if (!data || data.length === 0) {
          this.selectedGoals = [];
          return;
        }
        this.selectedGoals = data.filter(
          (goal: SavingsGoal) =>
            new Date(goal.deadline).toISOString().split('T')[0] === clickedDate
        );
      },
      error: (err) => {
        if (err.status === 404) {
          this.selectedGoals = [];
        } else {
          console.error('Error loading savings goals for date', err);
        }
      },
    });
  }

  calculateAvailableCapital(): void {
    const transactionData = { transactions: [] }; // Placeholder for transactions as []
    const transactions = transactionData.transactions;
    const income = transactions
      .filter((transaction: any) => {
        const date = new Date(transaction.date);
        return (
          transaction.type === 'Income' &&
          date.getMonth() === new Date().getMonth() &&
          date.getFullYear() === new Date().getFullYear()
        );
      })
      .reduce((sum: number, transaction: any) => sum + transaction.amount, 0);

    const expenses = transactions
      .filter((transaction: any) => {
        const date = new Date(transaction.date);
        return (
          transaction.type === 'Expense' &&
          date.getMonth() === new Date().getMonth() &&
          date.getFullYear() === new Date().getFullYear()
        );
      })
      .reduce((sum: number, transaction: any) => sum + transaction.amount, 0);

    this.availableCapital = 10000;

    this.savingsGoalService.getSavingsGoals().subscribe({
      next: (data: any) => {
        const savingsGoals = data || [];
        const totalCurrentAmount = savingsGoals.reduce((sum: number, goal: any) => sum + goal.currentAmount, 0);
        this.availableCapital -= totalCurrentAmount;
      },
      error: (err) => {
        if (err.status === 404) {
          console.log('No savings goals found for capital calculation.');
        } else {
          console.error('Error calculating available capital', err);
        }
      },
    });
  }

  prepareEditCurrentAmount(goal: any): void {
    this.goalId = goal.id;
    const { id, ...goalWithoutId } = goal;
    this.editGoal = goalWithoutId;
    //this.editGoal = goal;
  }

  submitGoal(): void {
    if (this.newGoal.currentAmount > this.availableCapital) {
      this.errorMessage = 'Current amount exceeds available capital.';
      setTimeout(() => (this.errorMessage = null), 3000);
      return;
    }

    if (!this.validateGoal()) {
      return;
    }

    if (this.isEditMode) {
      this.updateFullGoal();
    } else {
      this.createGoal();
    }


  }

  private validateGoal(): boolean {
    if (!this.newGoal.goalName) {
      this.errorMessage = 'Goal name is required.';
      setTimeout(() => (this.errorMessage = null), 3000);
      return false;
    }

    if (!this.isEditMode) {
      if (!this.newGoal.targetAmount || !this.newGoal.currentAmount || !this.newGoal.startDate || !this.newGoal.deadline) {
        this.errorMessage = 'All fields are required.';
        setTimeout(() => (this.errorMessage = null), 3000);
        return false;
      }

      if (this.newGoal.targetAmount <= 0) {
        this.errorMessage = 'Target amount must be greater than zero.';
        setTimeout(() => (this.errorMessage = null), 3000);
        return false;
      }

      if (this.newGoal.currentAmount <= 0) {
        this.errorMessage = 'Current amount must be greater than zero.';
        setTimeout(() => (this.errorMessage = null), 3000);
        return false;
      }

      if (new Date(this.newGoal.startDate) >= new Date(this.newGoal.deadline)) {
        this.errorMessage = 'Deadline must be after the start date.';
        setTimeout(() => (this.errorMessage = null), 3000);
        return false;
      }
    }

    return true;
  }

  private updateFullGoal(): void {
    this.savingsGoalService.updateSavingsGoal(this.goalId, this.newGoal).subscribe({
      next: () => {
        this.successMessage = 'Goal updated successfully.';
        setTimeout(() => (this.successMessage = ''), 3000);
        this.loadToCalendar();
        this.calculateAvailableCapital();
      },
      error: (err) => {
        console.error('Error updating goal', err);
      },
    });
  }

  private createGoal(): void {
    this.savingsGoalService.addSavingsGoal(this.newGoal).subscribe({
      next: () => {
        this.successMessage = 'Goal created successfully.';
        setTimeout(() => (this.successMessage = ''), 3000);
        this.loadToCalendar();
        this.calculateAvailableCapital();
      },
      error: (err) => {
        console.error('Error creating goal', err);
      },
    });
  }

  submitCurrentAmount(): void {
    //const updateData = { currentAmount: this.editGoal.currentAmount };
    this.savingsGoalService.updateSavingsGoal(this.goalId, this.editGoal).subscribe({
      next: () => {
        this.successMessage = 'Current amount updated successfully.';
        setTimeout(() => (this.successMessage = ''), 3000);
        this.loadToCalendar();
        this.calculateAvailableCapital();
      },
      error: (err) => {
        console.error('Error updating current amount', err);
      },
    });
  }

  prepareDeleteGoal(goalId: string): void {
    this.goalId = goalId;
  }

  deleteSavingsGoal(goalId: string): void {
    this.savingsGoalService.deleteSavingsGoal(goalId).subscribe({
      next: () => {
        this.successMessage = 'Goal deleted successfully.';
        setTimeout(() => (this.successMessage = ''), 3000);
        this.loadToCalendar();
        this.calculateAvailableCapital();
      },
      error: (err) => {
        console.error('Error deleting goal', err);
      },
    });
  }




}
