package ss.finance.services;

import ss.finance.entities.Report;
import ss.finance.entities.Transaction;
import ss.finance.entities.SavingsGoal;
import ss.finance.entities.Investment;
import ss.finance.services.GoalBean;
import ss.finance.services.InvestmentBean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReportBean {

    @Inject
    private TransactionBean transactionBean;

    @Inject
    private GoalBean goalBean;

    @Inject
    private InvestmentBean investmentBean;

    public Report generateCustomReport(String userId, LocalDate startDate, LocalDate endDate) {
        // Fetch Transactions for the user in the given period
        List<Transaction> transactions = transactionBean.getTransactionsByUserId(userId).stream()
                .filter(tx -> !tx.getDate().isBefore(startDate) && !tx.getDate().isAfter(endDate))
                .collect(Collectors.toList());

        // Calculate income and expenses
        double totalIncome = transactions.stream()
                .filter(tx -> "Income".equalsIgnoreCase(tx.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpenses = transactions.stream()
                .filter(tx -> "Expense".equalsIgnoreCase(tx.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Group expenses by category
        Map<String, Double> spendingByCategory = transactions.stream()
                .filter(tx -> "Expense".equalsIgnoreCase(tx.getType()))
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));

        // Group expenses by date
        Map<LocalDate, Map<String, Double>> spendingByDate = transactions.stream()
                .filter(tx -> "Expense".equalsIgnoreCase(tx.getType()))
                .collect(Collectors.groupingBy(Transaction::getDate, Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount))));

        // Fetch and summarize investments
        List<Investment> investments = investmentBean.getInvestmentsByUserId(userId);
        double totalProfitLoss = investments.stream()
                .mapToDouble(inv -> inv.getCurrentValue() - inv.getInitialAmount())
                .sum();
        List<Map<String, Object>> investmentDetails = investments.stream()
                .map(inv -> Map.of(
                        "name", inv.getName(),
                        "type", inv.getType(),
                        "profitLoss", inv.getCurrentValue() - inv.getInitialAmount()
                ))
                .collect(Collectors.toList());

        // Fetch and summarize savings goals
        List<SavingsGoal> savingsGoals = goalBean.getGoalsByUserId(userId);
        List<SavingsGoal> achievedGoals = savingsGoals.stream()
                .filter(goal -> goal.getCurrentAmount() >= goal.getTargetAmount())
                .collect(Collectors.toList());
        List<SavingsGoal> failedGoals = savingsGoals.stream()
                .filter(goal -> goal.getDeadline().isBefore(LocalDate.now()) && goal.getCurrentAmount() < goal.getTargetAmount())
                .collect(Collectors.toList());
        List<Map<String, Object>> ongoingGoals = savingsGoals.stream()
                .filter(goal -> goal.getDeadline().isAfter(LocalDate.now()) && goal.getCurrentAmount() < goal.getTargetAmount())
                .map(goal -> Map.of(
                        "goalName", goal.getName(),
                        "targetAmount", goal.getTargetAmount(),
                        "currentAmount", goal.getCurrentAmount(),
                        "progress", (goal.getCurrentAmount() / goal.getTargetAmount()) * 100 + "%"
                ))
                .collect(Collectors.toList());

        // Create report
        Report report = new Report();
        report.setUserId(userId);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalIncome(totalIncome);
        report.setTotalExpenses(totalExpenses);
        report.setNetSavings(totalIncome - totalExpenses);
        report.setSpendingByCategory(spendingByCategory);
        report.setSpendingByDate(spendingByDate);
        report.setInvestmentPerformance(Map.of(
                "totalProfitLoss", totalProfitLoss,
                "details", investmentDetails
        ));
        report.setSavingsGoals(Map.of(
                "achieved", achievedGoals,
                "failed", failedGoals,
                "ongoing", ongoingGoals
        ));

        return report;
    }
}