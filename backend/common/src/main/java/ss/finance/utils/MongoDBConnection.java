package ss.finance.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MongoDBConnection {
    private static MongoClient mongoClient;

    public static MongoClient getMongoClient() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create("mongodb://localhost:27017");
        }
        return mongoClient;
    }

    public static MongoDatabase getDatabase(String dbName) {
        return getMongoClient().getDatabase(dbName);
    }

}
