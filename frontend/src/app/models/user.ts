import { Transaction } from "./transaction";

export interface User {
    // Required Fields
    username: string; // Unique username
    fullName: string; // Full name of the user
    email: string; // Email address
    password: string; // Password

    // Optional Fields
    telephone?: string | null; // Telephone number, nullable
    language?: string; // Preferred language, defaults to 'en'
    notifications?: string[]; // Notification settings ('on' or 'off'), default to ['off', 'off', 'off', 'off']
    transactions?: Transaction[]; // Array of transactions (as strings)

    avatar?: string | null; // URL to profile picture, nullable
    dateOfBirth?: Date; // Date of birth
    currency?: string; // Preferred currency, defaults to 'EUR'
    savingsGoal?: number; // User's savings goal, defaults to 0
    recoveryEmail?: string | null; // Recovery email address, nullable

    // Timestamps
    createdAt?: Date; // Account creation date
    updatedAt?: Date; // Last account update date
    lastLogin?: Date; // Last login date

  }


