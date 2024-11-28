package ss.finance.services;

import java.time.LocalDate;

public class InvestmentDTO {

    private String id; // Unique ID (as a String for simplicity in DTOs)
    private String type; // Type of investment (e.g., Stock, Crypto)
    private String name; // Name of the investment (e.g., Bitcoin, Amazon)
    private double amount; // Total amount invested
    private double quantity; // Quantity of the investment
    private LocalDate purchaseDate; // Date of purchase
    private String userId; // ID of the user who owns the investment
    private Double currentValue; // Current value of the investment

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }
}
