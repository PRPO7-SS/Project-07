package ss.finance.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;
import ss.finance.entities.User;
import ss.finance.utils.MongoDBConnection;
import org.bson.types.ObjectId;
import org.bson.Document;


import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class UserBean {
    private MongoCollection<Document> collection;
    private static final Logger logger = Logger.getLogger(UserBean.class.getName());

    public UserBean() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        this.collection = database.getCollection("users");
    }

    public void addUser(User user) {
        try {
            Document existingUser = collection.find(new Document("email", user.getEmail())).first();
            if (existingUser != null) {
                throw new IllegalArgumentException("User with this email already exists.");
            }
            else if (!isValidEmail(user.getEmail())) {
                throw new IllegalArgumentException("Invalid email format.");
            }

            user.setCreatedAt(new java.util.Date());
            user.setUpdatedAt(new java.util.Date());

            Document userDoc = toDocument(user);
            collection.insertOne(userDoc);

            // Retrieve the generated ID and set it to the User object
            ObjectId generatedId = userDoc.getObjectId("_id");
            user.setId(generatedId);

            logger.info("User added successfully with ID: " + generatedId);
            logger.info("User added successfully: " + user.getEmail());
        } catch (Exception e) {
            logger.severe("Error adding user: " + e.getMessage());
            throw new RuntimeException("Error adding user", e);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public User validateUser(String email, String rawPassword) {
        Document doc = collection.find(new Document("email", email)).first();
        if (doc == null) {
            return null;
        }

        // Check password
        String hashedPassword = doc.getString("password");
        rawPassword = rawPassword.trim();
        logger.info(rawPassword);
        if (!BCrypt.checkpw(rawPassword, hashedPassword)) {
            logger.warning("Invalid login attempt: Incorrect password for email " + email);
            return null;
        }

        collection.updateOne(
                new Document("email", email),
                new Document("$set", new Document("lastLogin", new java.util.Date()))
        );

        return toUser(doc);
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        for (Document doc : collection.find()) {
            users.add(toUser(doc));
        }
        return users;
    }

    public void deleteUser(String userId) {
        collection.deleteOne(new Document("_id", userId));
    }

    public void updateUser(String userId, User updatedUser) {
        try {
            Document updateFields = toDocument(updatedUser);
            updateFields.append("updatedAt", new java.util.Date()); // Set updated timestamp

            collection.updateOne(
                    new Document("_id", new ObjectId(userId)),
                    new Document("$set", updateFields)
            );

            logger.info("User updated successfully with ID: " + userId);
        } catch (Exception e) {
            logger.severe("Error updating user: " + e.getMessage());
            throw new RuntimeException("Error updating user", e);
        }
    }


    private Document toDocument(User user) {
        Document document = new Document();
        if (user.getId() != null) {
            document.append("_id", user.getId()); // Preserve ID if explicitly set
        }
        return document.append("username", user.getUsername())
                .append("fullName", user.getFullName())
                .append("email", user.getEmail())
                .append("password", user.getPassword())
                .append("telephone", user.getTelephone())
                .append("language", user.getLanguage())
                .append("notifications", user.getNotifications())
                .append("avatar", user.getAvatar())
                .append("dateOfBirth", user.getDateOfBirth())
                .append("address", new Document("street", user.getAddress().getStreet())
                        .append("city", user.getAddress().getCity())
                        .append("postalCode", user.getAddress().getPostalCode())
                        .append("country", user.getAddress().getCountry()))
                .append("currency", user.getCurrency())
                .append("accountStatus", user.getAccountStatus())
                .append("theme", user.getTheme())
                .append("savingsGoal", user.getSavingsGoal())
                .append("income", user.getIncome())
                .append("twoFactorEnabled", user.isTwoFactorEnabled())
                .append("roles", user.getRoles())
                .append("createdAt", user.getCreatedAt())
                .append("updatedAt", user.getUpdatedAt())
                .append("lastLogin", user.getLastLogin());
    }

    private User toUser(Document doc) {
        User user = new User();
        user.setUsername(doc.getString("username"));
        user.setFullName(doc.getString("fullName"));
        user.setEmail(doc.getString("email"));
        user.setPassword(doc.getString("password"));
        user.setTelephone(doc.getString("telephone"));
        user.setLanguage(doc.getString("language"));
        user.setNotifications(doc.getList("notifications", String.class) != null ? doc.getList("notifications", String.class) : new ArrayList<>());
        user.setAvatar(doc.getString("avatar"));
        user.setDateOfBirth(doc.getDate("dateOfBirth"));

        User.Address address = new User.Address();
        Document addressDoc = doc.get("address", Document.class);
        if (addressDoc != null) {
            address.setStreet(addressDoc.getString("street"));
            address.setCity(addressDoc.getString("city"));
            address.setPostalCode(addressDoc.getString("postalCode"));
            address.setCountry(addressDoc.getString("country"));
        }
        user.setAddress(address);

        user.setCurrency(doc.getString("currency"));
        user.setAccountStatus(doc.getString("accountStatus"));
        user.setTheme(doc.getString("theme"));
        user.setSavingsGoal(doc.getDouble("savingsGoal"));
        user.setIncome(doc.getDouble("income"));
        user.setTwoFactorEnabled(doc.getBoolean("twoFactorEnabled"));
        user.setRoles(doc.getList("roles", String.class));
        user.setCreatedAt(doc.getDate("createdAt"));
        user.setUpdatedAt(doc.getDate("updatedAt"));
        user.setLastLogin(doc.getDate("lastLogin"));

        return user;
    }
}
