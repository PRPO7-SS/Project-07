package ss.finance.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ss.finance.entities.Transaction;
import ss.finance.utils.MongoDBConnection;

@ApplicationScoped
public class TransactionBean {
    private MongoCollection<Document> collection;
    private static final Logger logger = Logger.getLogger(TransactionBean.class.getName());

    public TransactionBean() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        this.collection = database.getCollection("transactions");
        logger.info("Initialized TransactionBean with MongoDB collection 'transactions'");
    }

    public void addTransaction(Transaction transaction) {
        try {
            logger.info("Adding transaction: " + transaction);
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
            logger.info("Fetching transactions for userId: " + userId);
            for (Document doc : collection.find(new Document("userId", userId))) {
                transactions.add(toTransaction(doc));
            }
            logger.info("Fetched " + transactions.size() + " transactions for userId: " + userId);
        } catch (Exception e) {
            logger.severe("Error retrieving transactions: " + e.getMessage());
            throw new RuntimeException("Error retrieving transactions", e);
        }
        return transactions;
    }

    public Transaction getTransactionById(ObjectId transactionId) {
        try {
            logger.info("Fetching transaction by ID: " + transactionId);
            Document doc = collection.find(new Document("_id", transactionId)).first();
            if (doc != null) {
                Transaction transaction = toTransaction(doc);
                logger.info("Fetched transaction: " + transaction);
                return transaction;
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
            logger.info("Attempting to delete transaction with ID: " + transactionId);
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
            logger.info("Updating transaction with ID: " + transactionId);
            logger.info("Updated transaction data: " + updatedTransaction);
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
        logger.info("Converting Transaction to Document: " + transaction);
        return new Document()
                .append("userId", transaction.getUserId())
                .append("type", transaction.getType())
                .append("amount", transaction.getAmount())
                .append("category", transaction.getCategory())
                .append("date", transaction.getDate())
                .append("currency", transaction.getCurrency())
                .append("createdAt", transaction.getCreatedAt())
                .append("updatedAt", transaction.getUpdatedAt());
    }

    private Transaction toTransaction(Document doc) {
        logger.info("Converting Document to Transaction: " + doc);
        Transaction transaction = new Transaction();
        transaction.setId(doc.getObjectId("_id"));
        transaction.setUserId(doc.getObjectId("userId"));
        transaction.setType(doc.getString("type"));
        transaction.setAmount(doc.getDouble("amount"));
        transaction.setCategory(doc.getString("category"));
        transaction.setDate(doc.getDate("date")); // Ensure this line is present
        transaction.setCurrency(doc.getString("currency"));
        transaction.setCreatedAt(doc.getDate("createdAt"));
        transaction.setUpdatedAt(doc.getDate("updatedAt"));
        logger.info("Converted Transaction: " + transaction);
        return transaction;
    }
}