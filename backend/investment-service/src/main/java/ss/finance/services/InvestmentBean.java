package ss.finance.services;

import java.time.LocalDate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import ss.finance.entities.Investment;
import ss.finance.utils.MongoDBConnection;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class InvestmentBean {

    private final MongoCollection<Document> collection;
    private static final Logger logger = Logger.getLogger(InvestmentBean.class.getName());

    public InvestmentBean() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        this.collection = database.getCollection("investments");
    }

    // Add a new investment
    public void addInvestment(Investment investment) {
        try {
            investment.setId(new ObjectId()); // Generate a new ID if not set
            Document investmentDoc = toDocument(investment);
            collection.insertOne(investmentDoc);
            logger.info("Investment added successfully: " + investment.getName());
        } catch (Exception e) {
            logger.severe("Error adding investment: " + e.getMessage());
            throw new RuntimeException("Error adding investment", e);
        }
    }

    // Get all investments
    public List<Investment> getAllInvestments() {
        List<Investment> investments = new ArrayList<>();
        for (Document doc : collection.find()) {
            investments.add(toInvestment(doc));
        }
        return investments;
    }

    // Get investment by ID
    public Investment getInvestmentById(String investmentId) {
        Document doc = collection.find(new Document("_id", new ObjectId(investmentId))).first();
        if (doc != null) {
            return toInvestment(doc);
        }
        return null;
    }

    // Update an existing investment
    public void updateInvestment(Investment investment) {
        try {
            Document updateFields = toDocument(investment);
            collection.updateOne(
                    new Document("_id", investment.getId()),
                    new Document("$set", updateFields)
            );
            logger.info("Investment updated successfully: " + investment.getName());
        } catch (Exception e) {
            logger.severe("Error updating investment: " + e.getMessage());
            throw new RuntimeException("Error updating investment", e);
        }
    }

    // Delete investment by ID
    public void deleteInvestment(String investmentId) {
        try {
            collection.deleteOne(new Document("_id", new ObjectId(investmentId)));
            logger.info("Investment deleted successfully with ID: " + investmentId);
        } catch (Exception e) {
            logger.severe("Error deleting investment: " + e.getMessage());
            throw new RuntimeException("Error deleting investment", e);
        }
    }

    // Helper method to convert Investment to Document
    private Document toDocument(Investment investment) {
        return new Document("_id", investment.getId())
                .append("type", investment.getType())
                .append("name", investment.getName())
                .append("amount", investment.getAmount())
                .append("quantity", investment.getQuantity())
                .append("purchaseDate", investment.getPurchaseDate().toString())
                .append("userId", investment.getUserId())
                .append("currentValue", investment.getCurrentValue());
    }

    // Helper method to convert Document to Investment
    private Investment toInvestment(Document doc) {
        Investment investment = new Investment();
        investment.setId(doc.getObjectId("_id"));
        investment.setType(doc.getString("type"));
        investment.setName(doc.getString("name"));
        investment.setAmount(doc.getDouble("amount"));
        investment.setQuantity(doc.getDouble("quantity"));
        investment.setPurchaseDate(LocalDate.parse(doc.getString("purchaseDate")));
        investment.setUserId(doc.getObjectId("userId"));
        investment.setCurrentValue(doc.getDouble("currentValue"));
        return investment;
    }
}