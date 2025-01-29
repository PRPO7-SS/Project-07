package ss.finance.utils;

import javax.enterprise.context.ApplicationScoped;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@ApplicationScoped
public class MongoDBConnection {
    private static MongoClient mongoClient;
    private static MongoClient testClient; // Dodatna spremenljivka za testiranje

    public static MongoClient getMongoClient() {
        if (testClient != null) {
            return testClient; // Uporabi testni MongoDB, če je nastavljen
        }

        if (mongoClient == null) {
            String mongoUri = System.getenv("MONGO_URI");
            if (mongoUri == null || mongoUri.isEmpty()) {
                mongoUri = "mongodb://localhost:27017"; // Privzeta lokalna povezava
            }
            mongoClient = MongoClients.create(mongoUri);
        }
        return mongoClient;
    }

    public static MongoDatabase getDatabase(String dbName) {
        return getMongoClient().getDatabase(dbName);
    }

    // ✅ DODANO: Metoda za testno injiciranje MongoDB klienta
    public static void setTestMongoClient(MongoClient testMongo) {
        testClient = testMongo;
    }
}