package ss.finance.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ss.finance.entities.User;
import ss.finance.utils.MongoDBConnection;

@ApplicationScoped
public class UserBean {
    private MongoCollection<Document> collection;
    private static final Logger logger = Logger.getLogger(UserBean.class.getName());
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserBean() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        this.collection = database.getCollection("users");
    }

    public void addUser(User user) {
        try {
            // Check if the user already exists by email
            Document existingUser = collection.find(new Document("email", user.getEmail())).first();

            if (existingUser != null) {
                // If the user exists, use their existing email as userId
                logger.info("User already exists with this email: " + user.getEmail());
            } else if (!isValidEmail(user.getEmail())) {
                throw new IllegalArgumentException("Invalid email format.");
            } else {
                user.setCreatedAt(new java.util.Date());
                user.setUpdatedAt(new java.util.Date());


                Document userDoc = toDocument(user);
                collection.insertOne(userDoc);

                // Log the successful user creation
                logger.info("User added successfully");
            }

            logger.info("User added successfully: " + user.getEmail());
        } catch (Exception e) {
            logger.severe("Error adding user: " + e.getMessage());
            throw new RuntimeException("Error adding user", e);
        }
    }

    public ObjectId getUserId(String email) {
        Document userDoc = collection.find(new Document("email", email)).first();
        if (userDoc != null) {
            // Access the _id field
            return userDoc.getObjectId("_id");
        } else {
            logger.severe("User not found for email: " + email);
            return null;
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

    public boolean existingUser(String email){
        Document doc = collection.find(new Document("email", email)).first();
        if(doc != null) return true;
        else return false;

    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        for (Document doc : collection.find()) {
            users.add(toUser(doc));
        }
        return users;
    }

    public boolean deleteUser(ObjectId userId) {
        Document existingUser = collection.find(new Document("_id", userId)).first();
        if (existingUser != null) {
            collection.deleteOne(new Document("_id", userId));
            return true;
        }
        else
            return false;
    }

    public void updateUser(ObjectId userId, User updatedUser) {
        try {
            Document updateFields = toDocument(updatedUser);
            updateFields.append("updatedAt", new java.util.Date()); // Set updated timestamp

            collection.updateOne(
                    new Document("_id", userId),
                    new Document("$set", updateFields)
            );

            logger.info("User updated successfully");
        } catch (Exception e) {
            logger.severe("Error updating user: " + e.getMessage());
            throw new RuntimeException("Error updating user", e);
        }
    }

    public User getUserById(ObjectId userId) {
        try {
            Document doc = collection.find(new Document("_id", userId)).first();
            if (doc != null) {
                return toUser(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving user by ID", e);
        }
        return null;
    }

    public User getUserByEmail(String email) {
        try {
            Document doc = collection.find(new Document("email", email)).first();
            if (doc != null) {
                return toUser(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving user by email", e);
        }
        return null;
    }

    public User getUserByResetToken(String resetToken) {
        try {
            Document doc = collection.find(new Document("resetToken", resetToken)).first();
            if (doc != null) {
                return toUser(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving user by email", e);
        }
        return null;
    }

    public ObjectId getUserId(User user) {
        try {
            // Query the database to find the user by email
            Document query = new Document("email", user.getEmail());
            Document userDocument = collection.find(query).first();

            if (userDocument == null) {
                throw new RuntimeException("User not found");
            }

            return userDocument.getObjectId("_id"); // Assuming the user ID is stored in the "_id" field
        } catch (Exception e) {
            logger.severe("Error retrieving user ID");
            throw new RuntimeException("Error retrieving user ID");
        }
    }



    private Document toDocument(User user) {
        Document document = new Document();
        return document.append("username", user.getUsername())
                .append("fullName", user.getFullName())
                .append("email", user.getEmail())
                .append("password", user.getPassword())
                .append("telephone", user.getTelephone())
                .append("language", user.getLanguage())
                .append("notifications", user.getNotifications())
                .append("avatar", user.getAvatar())
                .append("dateOfBirth", user.getDateOfBirth())
                .append("currency", user.getCurrency())
                .append("accountStatus", user.getAccountStatus())
                .append("savingsGoal", user.getSavingsGoal())
                .append("income", user.getIncome())
                .append("roles", user.getRoles())
                .append("createdAt", user.getCreatedAt())
                .append("updatedAt", user.getUpdatedAt())
                .append("lastLogin", user.getLastLogin())
                .append("resetToken", user.getResetToken())
                .append("resetTokenExpiry", user.getResetTokenExpiry());
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
        user.setCurrency(doc.getString("currency"));
        user.setAccountStatus(doc.getString("accountStatus"));
        user.setSavingsGoal(doc.getDouble("savingsGoal"));
        user.setIncome(doc.getDouble("income"));
        user.setRoles(doc.getList("roles", String.class));
        user.setCreatedAt(doc.getDate("createdAt"));
        user.setUpdatedAt(doc.getDate("updatedAt"));
        user.setLastLogin(doc.getDate("lastLogin"));
        user.setResetToken(doc.getString("resetToken"));
        user.setResetTokenExpiry(doc.getDate("resetTokenExpiry"));

        return user;
    }
}
