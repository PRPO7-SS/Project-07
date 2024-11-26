package ss.finance;
import org.bson.types.ObjectId;
import org.bson.Document;

import java.util.Date;

public class Transaction {
    private ObjectId id;
    private String type;
    private int amount;
    private String category;
    private Date date;

    public Transaction(String type, int amount, String category) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = new Date();
    }

    public Document toDocument() {
        return new Document("type", type)
                .append("amount", amount)
                .append("category", category)
                .append("date", date);
    }

    // Getters and Setters omitted for brevity
}
