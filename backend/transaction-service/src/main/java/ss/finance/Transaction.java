package ss.finance;
import org.bson.types.ObjectId;
import org.bson.Document;

import java.util.Date;

public class Transaction {
    private ObjectId userId;
    private String type;
    private int amount;
    private String category;
    private Date date;

    public Transaction(ObjectId userId, String type, int amount, String category) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = new Date();
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.append("userId", userId)
                .append("type", type)
                .append("amount", amount)
                .append("category", category);
        return doc;
    }

    // Getters and Setters omitted for brevity
}
