package ss.finance.core;

import com.kumuluz.ee.EeApplication;
import com.mongodb.client.MongoDatabase;

import ss.finance.utils.MongoDBConnection;


public class Main {
    public static void main(String[] args) {
        System.out.println("Application started!");
        MongoDatabase database = MongoDBConnection.getDatabase("financeApp");
        System.out.println("Connected to database: " + database.getName());
        EeApplication.main(args);
    }
}