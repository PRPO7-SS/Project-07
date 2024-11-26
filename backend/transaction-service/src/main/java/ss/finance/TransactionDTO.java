package ss.finance;
import java.util.Date;

public class TransactionDTO {
    private String type;
    private int amount;
    private String category;
    private Date date;

    public TransactionDTO() {}
    // Constructor
    public TransactionDTO(String type, int amount, String category, Date date) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
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
