package ss.finance.entities;

import java.util.Date;

import org.bson.types.ObjectId;

public class Transaction {
    private ObjectId id;
    private ObjectId userId;
    private String type; // "income" or "spending"
    private double amount;
    private String category; // e.g., "groceries", "salary"
    private Date date; // Date of the transaction
    private String currency; // e.g., "EUR"
    private Date createdAt;
    private Date updatedAt;

    public Transaction() {}

    public Transaction(ObjectId userId, String type, double amount, String category, Date date) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date != null ? date : new Date(); // Ensure the date is set
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
