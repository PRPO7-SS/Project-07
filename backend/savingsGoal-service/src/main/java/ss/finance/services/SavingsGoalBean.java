package ss.finance.services;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ss.finance.entities.SavingsGoal;
import ss.finance.utils.MongoDBConnection;

@ApplicationScoped
public class SavingsGoalBean {

    private final MongoCollection<Document> collection;
    private static final Logger logger = Logger.getLogger(SavingsGoalBean.class.getName());

    public SavingsGoalBean() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        this.collection = database.getCollection("savingsGoals");
    }

    // Add a new savings goal
    public void addSavingsGoal(SavingsGoal savingsGoal) {
        try {
            savingsGoal.setId(new ObjectId()); // Generate a new ID if not set
            Document savingsGoalDoc = toDocument(savingsGoal);
            collection.insertOne(savingsGoalDoc);
            logger.info("Savings goal added successfully: " + savingsGoal.getName());
        } catch (Exception e) {
            logger.severe("Error adding savings goal: " + e.getMessage());
            throw new RuntimeException("Error adding savings goal", e);
        }
    }

    // Get all savings goals
    public List<SavingsGoal> getAllSavingsGoals() {
        List<SavingsGoal> savingsGoals = new ArrayList<>();
        for (Document doc : collection.find()) {
            savingsGoals.add(toSavingsGoal(doc));
        }
        return savingsGoals;
    }

    // Get savings goal by ID
    public SavingsGoal getSavingsGoalById(String goalId) {
        Document doc = collection.find(new Document("_id", new ObjectId(goalId))).first();
        if (doc != null) {
            return toSavingsGoal(doc);
        }
        return null;
    }

    // Update an existing savings goal
    public void updateSavingsGoal(SavingsGoal savingsGoal) {
        try {
            Document updateFields = toDocument(savingsGoal);
            collection.updateOne(
                    new Document("_id", savingsGoal.getId()),
                    new Document("$set", updateFields)
            );
            logger.info("Savings goal updated successfully: " + savingsGoal.getName());
        } catch (Exception e) {
            logger.severe("Error updating savings goal: " + e.getMessage());
            throw new RuntimeException("Error updating savings goal", e);
        }
    }

    // Delete savings goal by ID
    public void deleteSavingsGoal(String goalId) {
        try {
            collection.deleteOne(new Document("_id", new ObjectId(goalId)));
            logger.info("Savings goal deleted successfully with ID: " + goalId);
        } catch (Exception e) {
            logger.severe("Error deleting savings goal: " + e.getMessage());
            throw new RuntimeException("Error deleting savings goal", e);
        }
    }

    // Helper method to convert SavingsGoal to Document
    private Document toDocument(SavingsGoal savingsGoal) {
        return new Document("_id", savingsGoal.getId())
                .append("name", savingsGoal.getName())
                .append("targetAmount", savingsGoal.getTargetAmount())
                .append("currentAmount", savingsGoal.getCurrentAmount())
                .append("targetDate", savingsGoal.getTargetDate().toString()) // Ensure targetDate is a String
                .append("userId", savingsGoal.getUserId())
                .append("description", savingsGoal.getDescription());
    }

    // Helper method to convert Document to SavingsGoal
    private SavingsGoal toSavingsGoal(Document doc) {
        SavingsGoal savingsGoal = new SavingsGoal();
        savingsGoal.setId(doc.getObjectId("_id"));
        savingsGoal.setGoalName(doc.getString("name"));
        savingsGoal.setTargetAmount(doc.getDouble("targetAmount"));
        savingsGoal.setCurrentAmount(doc.getDouble("currentAmount"));
        savingsGoal.setDeadline(LocalDate.parse(doc.getString("targetDate")));
        savingsGoal.setUserId(doc.getObjectId("userId"));
        savingsGoal.setDescription(doc.getString("description")); // Set the description here
        return savingsGoal;
    }

}