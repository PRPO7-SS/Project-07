package ss.finance;

import com.kumuluz.ee.EeApplication;
import com.mongodb.client.MongoDatabase;

import ss.finance.utils.MongoDBConnection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Investment service started!");
        MongoDatabase database = MongoDBConnection.getDatabase("investments");  // Povezava z bazo "investments"
        System.out.println("Connected to database: " + database.getName());
        EeApplication.main(args);  // Zaženi aplikacijo s KumuluzEE
    }
}