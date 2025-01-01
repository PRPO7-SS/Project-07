package ss.finance.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import ss.finance.entities.Investment;
import ss.finance.utils.MongoDBConnection;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class InvestmentBean {
    private MongoCollection<Document> collection;
    private static final Logger logger = Logger.getLogger(InvestmentBean.class.getName());

    public InvestmentBean() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        this.collection = database.getCollection("investments");
    }

    public void addInvestment(Investment investment) {
        try {
            Document investmentDoc = toDocument(investment);
            collection.insertOne(investmentDoc);
            logger.info("Investment added successfully");
        } catch (Exception e) {
            logger.severe("Error adding investment: " + e.getMessage());
            throw new RuntimeException("Error adding investment", e);
        }
    }

    public void updateInvestment(ObjectId investmentId, Investment updatedInvestment) {
        try {
            Document updateFields = toDocument(updatedInvestment);
            updateFields.append("updatedAt", new Date());
            collection.updateOne(
                    new Document("_id", investmentId),
                    new Document("$set", updateFields)
            );
            logger.info("Investment updated successfully");
        } catch (Exception e) {
            logger.severe("Error updating investment: " + e.getMessage());
            throw new RuntimeException("Error updating investment", e);
        }
    }

    public Investment getInvestment(ObjectId investmentId) {
        try {
            Document doc = collection.find(new Document("_id", investmentId)).first();
            if (doc != null) {
                return toInvestment(doc);
            }
        } catch (Exception e) {
            logger.severe("Error retrieving investment: " + e.getMessage());
            throw new RuntimeException("Error retrieving investment", e);
        }
        return null;
    }

    public List<Investment> getAllInvestments(ObjectId userId) {
        List<Investment> investments = new ArrayList<>();
        try {
            for (Document doc : collection.find(new Document("userId", userId))) {
                investments.add(toInvestment(doc));
            }
        } catch (Exception e) {
            logger.severe("Error retrieving all investments: " + e.getMessage());
            throw new RuntimeException("Error retrieving all investments", e);
        }
        return investments;
    }

    public boolean deleteInvestment(ObjectId investmentId) {
        try {
            collection.deleteOne(new Document("_id", investmentId));
            logger.info("Investment deleted successfully");
            return true;
        } catch (Exception e) {
            logger.severe("Error deleting investment: " + e.getMessage());
            return false;
        }
    }

    private Document toDocument(Investment investment) {
        return new Document()
                .append("userId", investment.getUserId())
                .append("type", investment.getType())
                .append("name", investment.getName())
                .append("amount", investment.getAmount())
                .append("quantity", investment.getQuantity())
                .append("purchaseDate", investment.getPurchaseDate())
                .append("currentValue", investment.getCurrentValue())
                .append("createdAt", new Date());
    }

    private Investment toInvestment(Document doc) {
        Investment investment = new Investment();
        investment.setId(doc.getObjectId("_id").toHexString());
        investment.setUserId(doc.getObjectId("userId"));
        investment.setType(doc.getString("type"));
        investment.setName(doc.getString("name"));
        investment.setAmount(doc.getInteger("amount"));
        investment.setQuantity(doc.getInteger("quantity"));
        investment.setPurchaseDate(doc.getDate("purchaseDate"));
        investment.setCurrentValue(doc.getInteger("currentValue"));
        return investment;
    }
}