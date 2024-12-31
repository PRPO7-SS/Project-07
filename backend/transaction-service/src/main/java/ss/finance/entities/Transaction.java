package ss.finance.entities;

import java.util.Date;

import org.bson.types.ObjectId;

public class Transaction {
    private ObjectId id;
    private ObjectId userId; // Reference to the user who made the transaction
    private String type; // income or spending
    private double amount; // Transaction amount
    private String category; // Transaction category
    private Date date; // Date of the transaction
    private String description; // Optional description of the transaction
    private String currency; // Currency of the transaction
    private Date createdAt; // Timestamp when the transaction was created
    private Date updatedAt; // Timestamp when the transaction was last updated

    public Transaction() {
        this.createdAt = new Date(); // Default createdAt to current date
        this.updatedAt = new Date(); // Default updatedAt to current date
    }

    public Transaction(ObjectId userId, String type, double amount, String category, String description, String currency) {
        this.id = new ObjectId();
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.currency = currency;
        this.date = new Date();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}