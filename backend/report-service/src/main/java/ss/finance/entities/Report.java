package ss.finance.entities;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Report {

    private String userId;
    private String type; // "custom" or "monthly"
    private Period period;
    private Data data;
    private Date createdAt;

    public Report(String userId, String type, Period period, Data data, Date createdAt) {
        this.userId = userId;
        this.type = type;
        this.period = period;
        this.data = data;
        this.createdAt = createdAt;
    }

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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public static class Period {
        private Date startDate;
        private Date endDate;

        public Period(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }
    }

    public static class Data {
        private TransactionSummary transactionSummary;
        private InvestmentPerformance investmentPerformance;
        private SavingsGoals savingsGoals;

        public Data(TransactionSummary transactionSummary, InvestmentPerformance investmentPerformance, SavingsGoals savingsGoals) {
            this.transactionSummary = transactionSummary;
            this.investmentPerformance = investmentPerformance;
            this.savingsGoals = savingsGoals;
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
    }

    public static class TransactionSummary {
        private double totalIncome;
        private double totalExpenses;
        private double netSavings;
        private Map<String, Double> spendingByCategory;
        private Map<String, Map<String, Double>> spendingByDate;
        private List<TransactionDetail> detailedTransactions;

        public TransactionSummary(double totalIncome, double totalExpenses, double netSavings, Map<String, Double> spendingByCategory, Map<String, Map<String, Double>> spendingByDate, List<TransactionDetail> detailedTransactions) {
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
            this.netSavings = netSavings;
            this.spendingByCategory = spendingByCategory;
            this.spendingByDate = spendingByDate;
            this.detailedTransactions = detailedTransactions;
        }

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
        private Date date;

        public TransactionDetail(String type, double amount, String category, Date date) {
            this.type = type;
            this.amount = amount;
            this.category = category;
            this.date = date;
        }

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

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    public static class InvestmentPerformance {
        private double totalProfitLoss;
        private List<InvestmentDetail> details;

        public InvestmentPerformance(double totalProfitLoss, List<InvestmentDetail> details) {
            this.totalProfitLoss = totalProfitLoss;
            this.details = details;
        }

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

        public InvestmentDetail(String name, String type, double profitLoss) {
            this.name = name;
            this.type = type;
            this.profitLoss = profitLoss;
        }

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
        private List<SavingsGoal> achieved;
        private List<SavingsGoal> failed;
        private List<SavingsGoal> ongoing;

        public SavingsGoals(List<SavingsGoal> achieved, List<SavingsGoal> failed, List<SavingsGoal> ongoing) {
            this.achieved = achieved;
            this.failed = failed;
            this.ongoing = ongoing;
        }

        public List<SavingsGoal> getAchieved() {
            return achieved;
        }

        public void setAchieved(List<SavingsGoal> achieved) {
            this.achieved = achieved;
        }

        public List<SavingsGoal> getFailed() {
            return failed;
        }

        public void setFailed(List<SavingsGoal> failed) {
            this.failed = failed;
        }

        public List<SavingsGoal> getOngoing() {
            return ongoing;
        }

        public void setOngoing(List<SavingsGoal> ongoing) {
            this.ongoing = ongoing;
        }
    }

    public static class SavingsGoal {
        private String goalName;
        private double targetAmount;
        private double currentAmount;
        private Date deadline;
        private String progress;

        public SavingsGoal(String goalName, double targetAmount, double currentAmount, Date deadline, String progress) {
            this.goalName = goalName;
            this.targetAmount = targetAmount;
            this.currentAmount = currentAmount;
            this.deadline = deadline;
            this.progress = progress;
        }

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

        public Date getDeadline() {
            return deadline;
        }

        public void setDeadline(Date deadline) {
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