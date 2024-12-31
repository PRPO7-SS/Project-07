package ss.finance.dtos;

import java.util.Date;

public class TransactionDTO {
    private String type;
    private double amount;
    private String category;
    private Date date; // Include the date field

    // Constructor with logging
    public TransactionDTO() {
        System.out.println("TransactionDTO initialized.");
    }

    // Getters and Setters
    public String getType() {
        System.out.println("Getting type: " + type);
        return type;
    }

    public void setType(String type) {
        System.out.println("Setting type: " + type);
        this.type = type;
    }

    public double getAmount() {
        System.out.println("Getting amount: " + amount);
        return amount;
    }

    public void setAmount(double amount) {
        System.out.println("Setting amount: " + amount);
        this.amount = amount;
    }

    public String getCategory() {
        System.out.println("Getting category: " + category);
        return category;
    }

    public void setCategory(String category) {
        System.out.println("Setting category: " + category);
        this.category = category;
    }

    public Date getDate() {
        System.out.println("Getting date: " + date);
        return date;
    }

    public void setDate(Date date) {
        System.out.println("Setting date: " + date);
        this.date = date;
    }

    // Override toString() for better logging
    @Override
    public String toString() {
        return "TransactionDTO{" +
                "type='" + type + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", date=" + date +
                '}';
    }
}