package ss.finance;

import com.kumuluz.ee.EeApplication;
import com.mongodb.client.MongoDatabase;
import ss.finance.utils.MongoDBConnection;



public class Main {
    public static void main(String[] args) {
        System.out.println("Application started!");
        MongoDatabase database = MongoDBConnection.getDatabase("users");
        System.out.println("Connected to database: " + database.getName());
        EeApplication.main(args);
    }
}