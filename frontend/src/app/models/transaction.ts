export interface Transaction {
    id: string;
    type: 'Income' | 'Expense';
    amount: number;
    date: string;
    category: string;
  }
