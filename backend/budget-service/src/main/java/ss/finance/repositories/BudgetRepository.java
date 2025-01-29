package ss.finance.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ss.finance.entities.Budget;
import ss.finance.utils.MongoDBConnection;

@ApplicationScoped
public class BudgetRepository {
    private MongoCollection<Document> collection;
    private static final Logger logger = Logger.getLogger(BudgetRepository.class.getName());

    public BudgetRepository() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        this.collection = database.getCollection("budgets");
        logger.info("Initialized BudgetRepository with MongoDB collection 'budgets'");
    }
    
    public void addBudget(Budget budget) {
        // Normalizacija kategorije (prva črka velika, ostale male)
        budget.setCategory(
            budget.getCategory().substring(0, 1).toUpperCase() + budget.getCategory().substring(1).toLowerCase()
        );

        Document budgetDoc = toDocument(budget);
        collection.insertOne(budgetDoc);
        logger.info("Budget added successfully: " + budget);
    }

    public List<Budget> getBudgetsByUserId(ObjectId userId) {
        List<Document> documents = collection.find(new Document("userId", userId)).into(new ArrayList<>());
        List<Budget> budgets = documents.stream().map(this::toBudget).collect(Collectors.toList());
        logger.info("Retrieved budgets for userId " + userId + ": " + budgets);
        return budgets;
    }

    public Budget getBudgetByUserIdAndCategory(ObjectId userId, String categoryName) {
        Document doc = collection.find(new Document("userId", userId).append("category", categoryName)).first();
        if (doc != null) {
            Budget budget = toBudget(doc);
            logger.info("Retrieved budget for userId " + userId + " and category " + categoryName + ": " + budget);
            return budget;
        } else {
            logger.warning("No budget found for userId " + userId + " and category " + categoryName);
            return null;
        }
    }

    public void updateMonthlyLimit(ObjectId budgetId, double newLimit) {
        var updateResult = collection.updateOne(
                new Document("_id", budgetId),
                new Document("$set", new Document("monthlyLimit", newLimit))
        );
        if (updateResult.getMatchedCount() > 0) {
            logger.info("Updated monthly limit for budgetId " + budgetId + " to " + newLimit);
        } else {
            logger.warning("No budget found with ID: " + budgetId);
        }
    }

    public boolean deleteBudget(ObjectId userId, String categoryName) {
        logger.info("Deleting budget with userId: " + userId + " and category: " + categoryName);
    
        // Uporaba `$regex` za ignoriranje velikih/malih črk
        var result = collection.deleteOne(new Document("userId", userId)
                .append("category", new Document("$regex", "^" + categoryName + "$").append("$options", "i")));
    
        if (result.getDeletedCount() > 0) {
            logger.info("Budget deleted successfully: userId=" + userId + ", category=" + categoryName);
            return true;
        } else {
            logger.warning("No budget found for userId: " + userId + " and category: " + categoryName);
            return false;
        }
    }

    private Document toDocument(Budget budget) {
        return new Document()
                .append("userId", budget.getUserId())
                .append("category", budget.getCategory())
                .append("monthlyLimit", budget.getMonthlyLimit());
    }

    private Budget toBudget(Document doc) {
        Budget budget = new Budget();
        budget.setId(doc.getObjectId("_id"));
        budget.setUserId(doc.getObjectId("userId"));
        budget.setCategory(doc.getString("category"));
        budget.setMonthlyLimit(doc.getDouble("monthlyLimit"));
        return budget;
    }
}