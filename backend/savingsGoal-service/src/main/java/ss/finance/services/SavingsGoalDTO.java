package ss.finance.services;

import java.time.LocalDate;

public class SavingsGoalDTO {

    private String id; // Unique ID (as a String for simplicity in DTOs)
    private String name; // Name of the savings goal (e.g., Retirement Fund, Vacation Fund)
    private double targetAmount; // The target amount for the savings goal
    private double currentAmount; // The current saved amount towards the goal
    private LocalDate targetDate; // The target date by which the goal should be reached
    private String userId; // ID of the user who owns the savings goal
    private String description; // A description of the savings goal

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}