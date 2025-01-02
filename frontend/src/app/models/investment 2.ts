
export interface Investment {
    // Required Fields
    id: string; // Unique identifier for the investment
    type: InvestmentType; // Type/category of the investment
    name: string; // Name of the investment
    amount: number; // Amount invested
    quantity: number;//quantity of stocks/coins....
    purchaseDate: Date; // Date of investment


    // Optional Fields
    description?: string; // Brief description of the investment
    status?: InvestmentStatus; // Current status of the investment
    returns?: number; // Returns generated from the investment
    riskLevel?: RiskLevel; // Risk level associated with the investment
    currency?: string; // Currency of the investment, defaults to 'USD'
    maturityDate?: Date; // Maturity date for investments like bonds or fixed deposits
    createdAt?: Date; // Investment creation date
    updatedAt?: Date; // Last update date

    // Nested Objects (if applicable)
    // For example, details about the investment manager or associated accounts
    manager?: InvestmentManager;
    account?: InvestmentAccount;
}

// Enumerations for strong typing
export type InvestmentType = 'stocks' | 'bonds' | 'mutual_funds' | 'real_estate' | 'cryptocurrency' | 'other';
export type InvestmentStatus = 'active' | 'closed' | 'pending' | 'canceled';
export type RiskLevel = 'low' | 'medium' | 'high';

// Nested Interface for Investment Manager
export interface InvestmentManager {
    name: string; // Manager's name
    contactEmail: string; // Manager's contact email
    phoneNumber?: string; // Manager's phone number, optional
}

// Nested Interface for Investment Account
export interface InvestmentAccount {
    accountId: string; // Unique account identifier
    accountType: 'brokerage' | 'retirement' | 'savings' | 'checking';
    bankName: string; // Name of the bank or brokerage
    accountNumber: string; // Account number
    routingNumber?: string; // Routing number, optional
}

// Request Interfaces for Creating and Updating Investments
export interface CreateInvestmentRequest {
    type: InvestmentType;
    name: string;
    amount: number;
    quantity: number;
    purchaseDate: Date;
    //description?: string;
    //manager?: InvestmentManager;
    //account?: InvestmentAccount;
}

export interface UpdateInvestmentRequest {
    name?: string;
    type?: InvestmentType;
    amount?: number;
    purchaseDate?: Date;
    description?: string;
    status?: InvestmentStatus;
    returns?: number;
    riskLevel?: RiskLevel;
    currency?: string;
    maturityDate?: Date;
    manager?: InvestmentManager;
    account?: InvestmentAccount;
}
