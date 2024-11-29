package ss.finance.entities;

import java.time.LocalDate;

import org.bson.types.ObjectId;

public class SavingsGoal {

    private ObjectId id; // Unique ID for the savings goal
    private String goalName; // Name of the savings goal (e.g., Summer 2025)
    private double targetAmount; // Target amount user wants to save
    private double currentAmount; // Current amount saved
    private LocalDate deadline; // Deadline for the savings goal
    private ObjectId userId; // Reference to the user who owns the savings goal
    private String description; // Description of the savings goal

    // Default constructor (required for MongoDB)
    public SavingsGoal() {
        this.id = new ObjectId(); // Automatically generate ObjectId
    }

    // Parameterized constructor
    public SavingsGoal(String goalName, double targetAmount, double currentAmount, LocalDate deadline, ObjectId userId) {
        this.id = new ObjectId();
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.userId = userId;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
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

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    // Add a method to get the target date (deadline)
    public LocalDate getTargetDate() {
        return this.deadline;  // Target date is the deadline of the goal
    }

    // Add a method to get a description (using goalName as the description)
    public String getDescription() {
        return this.goalName;  // Using goalName as the description of the savings goal
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "SavingsGoal{" +
                "id=" + id +
                ", goalName='" + goalName + '\'' +
                ", targetAmount=" + targetAmount +
                ", currentAmount=" + currentAmount +
                ", deadline=" + deadline +
                ", userId=" + userId +
                ", description='" + description + '\'' +
                '}';
    }

    // getName method to return the goal's name
    public String getName() {
        return this.goalName; // Returning goalName as the name
    }
}