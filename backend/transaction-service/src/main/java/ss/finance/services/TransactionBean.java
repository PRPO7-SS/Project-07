package ss.finance.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import ss.finance.entities.Transaction;
import ss.finance.utils.MongoDBConnection;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class TransactionBean {
    private MongoCollection<Document> collection;
    private static final Logger logger = Logger.getLogger(TransactionBean.class.getName());

    public TransactionBean() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        this.collection = database.getCollection("transactions");
    }

    public void addTransaction(Transaction transaction) {
        try {
            transaction.setCreatedAt(new Date());
            transaction.setUpdatedAt(new Date());

            Document transactionDoc = toDocument(transaction);
            collection.insertOne(transactionDoc);

            logger.info("Transaction added successfully: " + transaction.getType());
        } catch (Exception e) {
            logger.severe("Error adding transaction: " + e.getMessage());
            throw new RuntimeException("Error adding transaction", e);
        }
    }

    public List<Transaction> getTransactionsByUserId(ObjectId userId) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            for (Document doc : collection.find(new Document("userId", userId))) {
                transactions.add(toTransaction(doc));
            }
        } catch (Exception e) {
            logger.severe("Error retrieving transactions: " + e.getMessage());
            throw new RuntimeException("Error retrieving transactions", e);
        }
        return transactions;
    }

    public Transaction getTransactionById(ObjectId transactionId) {
        try {
            Document doc = collection.find(new Document("_id", transactionId)).first();
            if (doc != null) {
                return toTransaction(doc);
            } else {
                logger.warning("Transaction not found with ID: " + transactionId);
                return null;
            }
        } catch (Exception e) {
            logger.severe("Error retrieving transaction: " + e.getMessage());
            throw new RuntimeException("Error retrieving transaction", e);
        }
    }

    public boolean deleteTransaction(ObjectId transactionId) {
        try {
            Document existingTransaction = collection.find(new Document("_id", transactionId)).first();
            if (existingTransaction != null) {
                collection.deleteOne(new Document("_id", transactionId));
                logger.info("Transaction deleted successfully: " + transactionId);
                return true;
            } else {
                logger.warning("Transaction not found for deletion: " + transactionId);
                return false;
            }
        } catch (Exception e) {
            logger.severe("Error deleting transaction: " + e.getMessage());
            throw new RuntimeException("Error deleting transaction", e);
        }
    }

    public void updateTransaction(ObjectId transactionId, Transaction updatedTransaction) {
        try {
            Document updateFields = toDocument(updatedTransaction);
            updateFields.append("updatedAt", new Date());

            collection.updateOne(
                new Document("_id", transactionId),
                new Document("$set", updateFields)
            );

            logger.info("Transaction updated successfully: " + transactionId);
        } catch (Exception e) {
            logger.severe("Error updating transaction: " + e.getMessage());
            throw new RuntimeException("Error updating transaction", e);
        }
    }

    private Document toDocument(Transaction transaction) {
        return new Document()
                .append("userId", transaction.getUserId())
                .append("type", transaction.getType())
                .append("amount", transaction.getAmount())
                .append("category", transaction.getCategory())
                .append("currency", transaction.getCurrency())
                .append("createdAt", transaction.getCreatedAt())
                .append("updatedAt", transaction.getUpdatedAt());
    }

    private Transaction toTransaction(Document doc) {
        Transaction transaction = new Transaction();
        transaction.setId(doc.getObjectId("_id"));
        transaction.setUserId(doc.getObjectId("userId"));
        transaction.setType(doc.getString("type"));
        transaction.setAmount(doc.getDouble("amount"));
        transaction.setCategory(doc.getString("category"));
        transaction.setCurrency(doc.getString("currency"));
        transaction.setCreatedAt(doc.getDate("createdAt"));
        transaction.setUpdatedAt(doc.getDate("updatedAt"));
        return transaction;
    }
}