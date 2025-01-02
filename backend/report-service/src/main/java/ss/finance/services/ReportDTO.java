package ss.finance.dto;

import java.util.List;
import java.util.Map;

public class ReportDTO {

    private String userId;
    private String type;
    private Period period;
    private TransactionSummary transactionSummary;
    private InvestmentPerformance investmentPerformance;
    private SavingsGoals savingsGoals;

    public ReportDTO() {
    }

    // Getters and Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public TransactionSummary getTransactionSummary() {
        return transactionSummary;
    }

    public void setTransactionSummary(TransactionSummary transactionSummary) {
        this.transactionSummary = transactionSummary;
    }

    public InvestmentPerformance getInvestmentPerformance() {
        return investmentPerformance;
    }

    public void setInvestmentPerformance(InvestmentPerformance investmentPerformance) {
        this.investmentPerformance = investmentPerformance;
    }

    public SavingsGoals getSavingsGoals() {
        return savingsGoals;
    }

    public void setSavingsGoals(SavingsGoals savingsGoals) {
        this.savingsGoals = savingsGoals;
    }

    // Inner Classes for DTO structure

    public static class Period {
        private String startDate;
        private String endDate;

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }

    public static class TransactionSummary {
        private double totalIncome;
        private double totalExpenses;
        private double netSavings;
        private Map<String, Double> spendingByCategory;
        private Map<String, Map<String, Double>> spendingByDate;
        private List<TransactionDetail> detailedTransactions;

        public double getTotalIncome() {
            return totalIncome;
        }

        public void setTotalIncome(double totalIncome) {
            this.totalIncome = totalIncome;
        }

        public double getTotalExpenses() {
            return totalExpenses;
        }

        public void setTotalExpenses(double totalExpenses) {
            this.totalExpenses = totalExpenses;
        }

        public double getNetSavings() {
            return netSavings;
        }

        public void setNetSavings(double netSavings) {
            this.netSavings = netSavings;
        }

        public Map<String, Double> getSpendingByCategory() {
            return spendingByCategory;
        }

        public void setSpendingByCategory(Map<String, Double> spendingByCategory) {
            this.spendingByCategory = spendingByCategory;
        }

        public Map<String, Map<String, Double>> getSpendingByDate() {
            return spendingByDate;
        }

        public void setSpendingByDate(Map<String, Map<String, Double>> spendingByDate) {
            this.spendingByDate = spendingByDate;
        }

        public List<TransactionDetail> getDetailedTransactions() {
            return detailedTransactions;
        }

        public void setDetailedTransactions(List<TransactionDetail> detailedTransactions) {
            this.detailedTransactions = detailedTransactions;
        }
    }

    public static class TransactionDetail {
        private String type;
        private double amount;
        private String category;
        private String date;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    public static class InvestmentPerformance {
        private double totalProfitLoss;
        private List<InvestmentDetail> details;

        public double getTotalProfitLoss() {
            return totalProfitLoss;
        }

        public void setTotalProfitLoss(double totalProfitLoss) {
            this.totalProfitLoss = totalProfitLoss;
        }

        public List<InvestmentDetail> getDetails() {
            return details;
        }

        public void setDetails(List<InvestmentDetail> details) {
            this.details = details;
        }
    }

    public static class InvestmentDetail {
        private String name;
        private String type;
        private double profitLoss;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getProfitLoss() {
            return profitLoss;
        }

        public void setProfitLoss(double profitLoss) {
            this.profitLoss = profitLoss;
        }
    }

    public static class SavingsGoals {
        private List<Goal> achieved;
        private List<Goal> failed;
        private List<Goal> ongoing;

        public List<Goal> getAchieved() {
            return achieved;
        }

        public void setAchieved(List<Goal> achieved) {
            this.achieved = achieved;
        }

        public List<Goal> getFailed() {
            return failed;
        }

        public void setFailed(List<Goal> failed) {
            this.failed = failed;
        }

        public List<Goal> getOngoing() {
            return ongoing;
        }

        public void setOngoing(List<Goal> ongoing) {
            this.ongoing = ongoing;
        }
    }

    public static class Goal {
        private String goalName;
        private double targetAmount;
        private double currentAmount;
        private String deadline;
        private String progress;

        public String getGoalName() {
            return goalName;
        }

        public void setGoalName(String goalName) {
            this.goalName = goalName;
        }

        public double getTargetAmount() {
            return targetAmount;
        }

        public void setTargetAmount(double targetAmount) {
            this.targetAmount = targetAmount;
        }

        public double getCurrentAmount() {
            return currentAmount;
        }

        public void setCurrentAmount(double currentAmount) {
            this.currentAmount = currentAmount;
        }

        public String getDeadline() {
            return deadline;
        }

        public void setDeadline(String deadline) {
            this.deadline = deadline;
        }

        public String getProgress() {
            return progress;
        }

        public void setProgress(String progress) {
            this.progress = progress;
        }
    }
}