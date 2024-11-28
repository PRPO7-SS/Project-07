package ss.finance.entities;

import java.time.LocalDate;

import org.bson.types.ObjectId;

public class Investment {

    private ObjectId id; // Unique ID for the investment
    private String type; // Type of investment (e.g., Stock, Crypto)
    private String name; // Name of the investment (e.g., Bitcoin, Amazon)
    private double amount; // Total invested amount
    private double quantity; // Quantity of the investment
    private LocalDate purchaseDate; // Date when the investment was made
    private ObjectId userId; // Reference to the user who owns the investment
    private Double currentValue; // Current value of the investment

    // Default constructor (required for MongoDB)
    public Investment() {
        this.id = new ObjectId(); // Automatically generate ObjectId
    }

    // Parameterized constructor
    public Investment(String type, String name, double amount, double quantity, LocalDate purchaseDate, ObjectId userId, Double currentValue) {
        this.id = new ObjectId();
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.userId = userId;
        this.currentValue = currentValue;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    @Override
    public String toString() {
        return "Investment{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", quantity=" + quantity +
                ", purchaseDate=" + purchaseDate +
                ", userId=" + userId +
                ", currentValue=" + currentValue +
                '}';
    }
}