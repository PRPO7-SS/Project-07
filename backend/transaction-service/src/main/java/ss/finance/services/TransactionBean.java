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
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.jsonwebtoken.io.IOException;
import ss.finance.entities.Transaction;
import ss.finance.rabbit.RabbitMQConfig;
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
            // Validate required fields
            if (transaction.getType() == null || transaction.getType().isEmpty() ||
                transaction.getCategory() == null || transaction.getCategory().isEmpty() ||
                transaction.getAmount() <= 0) {
                throw new IllegalArgumentException("Type, category, and amount are required fields.");
            }
    
            // Ensure the date is set
            if (transaction.getDate() == null) {
                transaction.setDate(new Date());
            }
    
            // Set creation and update timestamps
            transaction.setCreatedAt(new Date());
            transaction.setUpdatedAt(new Date());
    
            Document transactionDoc = toDocument(transaction);
            logger.info("Saving transaction to the database: " +
                "userId=" + transaction.getUserId() +
                ", type=" + transaction.getType() +
                ", amount=" + transaction.getAmount() +
                ", category=" + transaction.getCategory() +
                ", date=" + transaction.getDate());
            collection.insertOne(transactionDoc);
    
            logger.info("Transaction added successfully: " + transaction);

            // Send the transaction to RabbitMQ
            sendTransactionToQueue(transaction);
        } catch (IllegalArgumentException e) {
            logger.warning("Validation error: " + e.getMessage());
            throw new RuntimeException("Validation error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.severe("Error adding transaction: " + e.getMessage());
            throw new RuntimeException("Error adding transaction", e);
        }
    }
    
    public void sendTransactionToQueue(Transaction transaction) {
        logger.info("Preparing to send transaction to RabbitMQ queue...");
    
        // Debug transaction details
        logger.info("Transaction details: userId=" + transaction.getUserId() +
                    ", type=" + transaction.getType() +
                    ", amount=" + transaction.getAmount() +
                    ", category=" + transaction.getCategory() +
                    ", date=" + transaction.getDate());
    
        try (Connection connection = RabbitMQConfig.createConnection();
             Channel channel = connection.createChannel()) {
    
            logger.info("RabbitMQ connection established successfully.");
    
            // Declare the queue
            String queueName = RabbitMQConfig.getQueueName();
            logger.info("Declaring queue with name: " + queueName);
            channel.queueDeclare(queueName, false, false, false, null);
            logger.info("Queue declared successfully: " + queueName);
    
            // Retrieve queue details
            logger.info("Fetching queue details for: " + queueName);
            try {
                channel.queueDeclarePassive(queueName);
                logger.info("Queue '" + queueName + "' exists and is ready to receive messages.");
            } catch (IOException e) {
                logger.warning("Queue '" + queueName + "' does not exist or is not accessible: " + e.getMessage());
            }
    
            // Convert transaction to JSON-like string
            String message = String.format("{ \"userId\": \"%s\", \"type\": \"%s\", \"amount\": %.2f, \"category\": \"%s\", \"date\": \"%s\" }",
                    transaction.getUserId(),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getCategory(),
                    transaction.getDate());
    
            logger.info("Message to be sent: " + message);
    
            // Publish the message
            logger.info("Publishing message to queue: " + queueName);
            channel.basicPublish("", queueName, null, message.getBytes());
            logger.info("Message published to queue '" + queueName + "': " + message);
    
        } catch (Exception e) {
            logger.severe("Failed to send message to RabbitMQ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Transaction> getTransactionsByUserId(ObjectId userId) {
        logger.info("Querying database for transactions with userId: " + userId);
    
        List<Transaction> transactions = collection.find(new Document("userId", userId))
                .into(new ArrayList<>())
                .stream().map(doc->toTransaction(doc)).toList();
    
        for (Transaction transaction : transactions) {
            logger.info("Transaction retrieved from database: " +
                "userId=" + transaction.getUserId() +
                ", type=" + transaction.getType() +
                ", amount=" + transaction.getAmount() +
                ", category=" + transaction.getCategory() +
                ", date=" + transaction.getDate());
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
        Document doc = new Document()
                .append("userId", transaction.getUserId())
                .append("type", transaction.getType())
                .append("amount", transaction.getAmount())
                .append("category", transaction.getCategory())
                .append("date", transaction.getDate()) // Log the date field
                .append("createdAt", transaction.getCreatedAt())
                .append("updatedAt", transaction.getUpdatedAt());
        logger.info("Generated Document: " + doc);
        return doc;
    }
    
    private Transaction toTransaction(Document doc) {
        logger.info("Converting Document to Transaction: " + doc);
        Transaction transaction = new Transaction();
        transaction.setId(doc.getObjectId("_id"));
        transaction.setUserId(doc.getObjectId("userId"));
        transaction.setType(doc.getString("type"));
        try {
            transaction.setAmount(doc.getDouble("amount"));
        } catch (ClassCastException e) {
            transaction.setAmount(doc.getInteger("amount"));
        }
        transaction.setCategory(doc.getString("category"));
        transaction.setDate(doc.getDate("date")); // Ensure this field is logged
        transaction.setCreatedAt(doc.getDate("createdAt"));
        transaction.setUpdatedAt(doc.getDate("updatedAt"));
        logger.info("Converted Transaction: " + transaction);
        return transaction;
    }    
}