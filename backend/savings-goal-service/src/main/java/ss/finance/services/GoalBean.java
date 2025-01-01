package ss.finance.services;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import ss.finance.entities.SavingsGoal;
import ss.finance.utils.MongoDBConnection;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class GoalBean {
    private MongoCollection<Document> collection;
    private static final Logger logger = Logger.getLogger(GoalBean.class.getName());

    public GoalBean() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        this.collection = database.getCollection("savings-goals");
    }

    public void addSavingsGoal(SavingsGoal goal) {
        try {
            Document goalDoc = toDocument(goal);
            collection.insertOne(goalDoc);
            logger.info("Savings goal added successfully");
        } catch (Exception e) {
            logger.severe("Error adding savings goal: " + e.getMessage());
            throw new RuntimeException("Error adding savings goal", e);
        }
    }

    public void updateSavingsGoal(ObjectId goalId, SavingsGoal updatedGoal) {
        try {
            Document updateFields = toDocument(updatedGoal);
            updateFields.append("updatedAt", new Date());
            collection.updateOne(
                    new Document("_id", goalId),
                    new Document("$set", updateFields)
            );
            logger.info("Savings goal updated successfully");
        } catch (Exception e) {
            logger.severe("Error updating savings goal: " + e.getMessage());
            throw new RuntimeException("Error updating savings goal", e);
        }
    }

    public SavingsGoal getSavingsGoal(ObjectId goalId) {
        try {
            Document doc = collection.find(new Document("_id", goalId)).first();
            if (doc != null) {
                return toSavingsGoal(doc);
            }
        } catch (Exception e) {
            logger.severe("Error retrieving savings goal: " + e.getMessage());
            throw new RuntimeException("Error retrieving savings goal", e);
        }
        return null;
    }

    public List<SavingsGoal> getAllSavingsGoals(ObjectId userId) {
        List<SavingsGoal> goals = new ArrayList<>();
        try {
            for (Document doc : collection.find(new Document("userId", userId))) {
                goals.add(toSavingsGoal(doc));
            }
        } catch (Exception e) {
            logger.severe("Error retrieving all savings goals: " + e.getMessage());
            throw new RuntimeException("Error retrieving all savings goals", e);
        }
        return goals;
    }

    public boolean deleteSavingsGoal(ObjectId goalId) {
        try {
            collection.deleteOne(new Document("_id", goalId));
            logger.info("Savings goal deleted successfully");
            return true;
        } catch (Exception e) {
            logger.severe("Error deleting savings goal: " + e.getMessage());
            return false;
        }
    }

    private Document toDocument(SavingsGoal goal) {
        return new Document()
                .append("userId", new ObjectId(goal.getUserId()))
                .append("goalName", goal.getGoalName())
                .append("targetAmount", goal.getTargetAmount())
                .append("currentAmount", goal.getCurrentAmount())
                .append("startDate", goal.getStartDate())
                .append("deadline", goal.getDeadline())
                .append("createdAt", new Date());
    }

    private SavingsGoal toSavingsGoal(Document doc) {
        SavingsGoal goal = new SavingsGoal();
        goal.setId(doc.getObjectId("_id").toHexString());
        goal.setUserId(doc.getObjectId("userId"));
        goal.setGoalName(doc.getString("goalName"));
        goal.setTargetAmount(doc.getInteger("targetAmount"));
        goal.setCurrentAmount(doc.getInteger("currentAmount"));
        goal.setStartDate(doc.getDate("startDate"));
        goal.setDeadline(doc.getDate("deadline"));
        return goal;
    }
}