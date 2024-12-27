package ss.finance;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import ss.finance.utils.MongoDBConnection;
import org.bson.Document;
import javax.inject.Inject;
import org.bson.types.ObjectId;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TransactionZrno {
    private MongoCollection<Document> collection;

    public TransactionZrno() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        this.collection = database.getCollection("transactions");
    }

    public void addTransaction(Transaction transaction) {

        collection.insertOne(transaction.toDocument());
    }

    public List<TransactionDTO> getAllTransactions(ObjectId userId) {
        List<TransactionDTO> transactions = new ArrayList<>();
        for (Document doc : collection.find(new Document("userId",  userId))) {
            TransactionDTO transactionDTO = new TransactionDTO(
                    doc.getObjectId("userId"),
                    doc.getString("type"),
                    doc.getInteger("amount"),
                    doc.getString("category"),
                    doc.getDate("date")
            );
            transactions.add(transactionDTO);
        }
        return transactions;
    }

}
