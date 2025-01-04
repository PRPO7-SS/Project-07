package ss.finance.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ss.finance.entities.Debt;
import ss.finance.utils.MongoDBConnection;

@ApplicationScoped
public class DebtRepository {

    private MongoCollection<Document> collection;
    private static final Logger logger = Logger.getLogger(DebtRepository.class.getName());

    public DebtRepository() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        this.collection = database.getCollection("debts");
        logger.info("Initialized DebtRepository with MongoDB collection 'debts'");
    }

    public void addDebt(Debt debt) {
        Document debtDoc = toDocument(debt);
        collection.insertOne(debtDoc);
        logger.info("Debt added successfully: " + debt);
    }

    public List<Debt> getDebtsByUserId(ObjectId userId) {
        List<Debt> debts = collection.find(new Document("userId", userId))
                .into(new ArrayList<>())
                .stream()
                .map(this::toDebt)
                .toList();
        logger.info("Retrieved debts for userId " + userId + ": " + debts);
        return debts;
    }

    public Debt getDebtById(ObjectId debtObjectId) {
        try {
            // Find the document by its ObjectId
            Document document = collection.find(new Document("_id", debtObjectId)).first();
            if (document != null) {
                logger.info("Debt retrieved successfully: " + debtObjectId);
                return toDebt(document);
            } else {
                logger.warning("No debt found with ID: " + debtObjectId);
                return null;
            }
        } catch (Exception e) {
            logger.severe("Error retrieving debt with ID " + debtObjectId + ": " + e.getMessage());
            throw new RuntimeException("Error retrieving debt", e);
        }
    }

    public boolean updateDebt(ObjectId debtId, Debt updatedDebt) {
        try {
            // Convert the updatedDebt object to a Document
            Document updateFields = new Document()
                    .append("creditor", updatedDebt.getCreditor())
                    .append("description", updatedDebt.getDescription())
                    .append("amount", updatedDebt.getAmount())
                    .append("isPaid", updatedDebt.getIsPaid())
                    .append("deadline", updatedDebt.getDeadline());
    
            var result = collection.updateOne(
                    new Document("_id", debtId),
                    new Document("$set", updateFields)
            );
    
            if (result.getMatchedCount() > 0) {
                logger.info("Successfully updated debt with ID: " + debtId);
                return true;
            } else {
                logger.warning("No debt found with ID: " + debtId);
                return false;
            }
        } catch (Exception e) {
            logger.severe("Error updating debt with ID " + debtId + ": " + e.getMessage());
            throw new RuntimeException("Error updating debt", e);
        }
    }

    public boolean deleteDebt(ObjectId debtId) {
        var result = collection.deleteOne(new Document("_id", debtId));
        if (result.getDeletedCount() > 0) {
            logger.info("Debt deleted successfully: " + debtId);
            return true;
        } else {
            logger.warning("No debt found with ID: " + debtId);
            return false;
        }
    }

    public void markAsPaid(ObjectId debtId) {
        var updateResult = collection.updateOne(
                new Document("_id", debtId),
                new Document("$set", new Document("isPaid", true))
        );
        if (updateResult.getMatchedCount() > 0) {
            logger.info("Marked debt with ID " + debtId + " as paid.");
        } else {
            logger.warning("No debt found with ID: " + debtId);
        }
    }

    private Document toDocument(Debt debt) {
        return new Document()
                .append("userId", debt.getUserId())
                .append("creditor", debt.getCreditor())
                .append("description", debt.getDescription())
                .append("amount", debt.getAmount())
                .append("isPaid", debt.getIsPaid())
                .append("deadline", debt.getDeadline());
    }

    private Debt toDebt(Document doc) {
        Debt debt = new Debt();
        debt.setId(doc.getObjectId("_id"));
        debt.setUserId(doc.getObjectId("userId"));
        debt.setCreditor(doc.getString("creditor"));
        debt.setDescription(doc.getString("description"));
        debt.setAmount(doc.getDouble("amount"));
        debt.setIsPaid(doc.getBoolean("isPaid"));
        debt.setDeadline(doc.getDate("deadline"));
        return debt;
    }
}