export interface SavingsGoal {
    goalName: string;
    targetAmount: number;
    currentAmount: number;
    startDate: Date;
    deadline: Date;
    userId: string;
}