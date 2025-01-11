package ss.finance.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;

import ss.finance.entities.Investment;
import ss.finance.rabbit.RabbitMQConfig;
import ss.finance.utils.MongoDBConnection;

@ApplicationScoped
public class InvestmentBean {
    private MongoCollection<Document> collection;
    private static final Logger logger = Logger.getLogger(InvestmentBean.class.getName());
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();


    public InvestmentBean() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        this.collection = database.getCollection("investments");
    }

    @PostConstruct
    public void init() {
        try {
            logger.info("Delaying RabbitMQ listener initialization for 10 seconds...");
            Thread.sleep(10000); // Delay to ensure RabbitMQ is ready
            logger.info("Initializing RabbitMQ listener for InvestmentService...");
            startRabbitMQConsumer();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning("Initialization delay was interrupted: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Failed to initialize RabbitMQ listener: " + e.getMessage());
        }
    }

    public void startRabbitMQConsumer() {
        try (Connection connection = RabbitMQConfig.createConnection();
            Channel channel = connection.createChannel()) {

            String queueName = RabbitMQConfig.getQueueName();
            logger.info("Connecting to RabbitMQ queue: " + queueName);

            while (true) {
                try {
                    // Pridobi naslednje sporočilo iz vrste, če obstaja
                    GetResponse response = channel.basicGet(queueName, true); // true za avtomatsko potrjevanje

                    if (response != null) {
                        String message = new String(response.getBody(), "UTF-8");
                        logger.info("Pulled message: " + message);
                        processMessage(message); // Obdelava prejetega sporočila
                    } else {
                        logger.info("No messages in the queue.");
                    }

                    // Počakajte 5 sekund pred naslednjim preverjanjem
                    int sleepTime = (response != null) ? 1000 : 5000;
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    logger.warning("Thread interrupted while sleeping: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Ponastavite zastavico prekinjenega niti
                } catch (Exception e) {
                    logger.severe("Error while processing messages: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            logger.severe("Error connecting to RabbitMQ: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processMessage(String message) {
        logger.info("Received a new transaction: " + message);
        logger.info("RabbitMQ messages work! 😊");
        logger.info("Thank you for using our service! 😊");
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
                Investment investment = toInvestment(doc);
                double price = calculateCurrentPrice(investment);
                investment.setCurrentPrice(price);
                double value = calculateCurrentValue(investment);
                investment.setCurrentValue(value);
                investments.add(investment);
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

    public Double calculateCurrentPrice(Investment i){
        String baseUrl = System.getenv("API_URL");
        String apiKey = System.getenv("API_KEY");
        String type = i.getType();
        String symbol = i.getName();
        String url = "";

        if(type.equals("stock")){
            url = String.format("%s/price?symbol=%s,USD/EUR&apikey=%s", baseUrl, symbol, apiKey);
        }else if(type.equals("crypto")){
            url = String.format("%s/price?symbol=%s/EUR&apikey=%s", baseUrl, symbol, apiKey);
        }else{
            return (double)i.getAmount();
        }

        System.out.println("url: " + url);

        try {
            URL apiUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Failed to fetch data: HTTP error code " + responseCode);
            }

            StringBuilder inline = new StringBuilder();
            Scanner scanner = new Scanner(apiUrl.openStream());
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject response = new JSONObject(inline.toString());

            double priceInEur;
            if (type.equals("stock")) {
                double priceInUsd = response.getJSONObject(symbol).getDouble("price");
                double usdToEur = response.getJSONObject("USD/EUR").getDouble("price");
                priceInEur = priceInUsd * usdToEur;
            } else {
                priceInEur = response.getDouble("price");
            }

            System.out.println("price in eur: " + priceInEur);

            return priceInEur;

        } catch (Exception e) {
            logger.severe("Error while calculating current value"+ e.getMessage());
            return null;
        }
    }

    public Double calculateCurrentValue(Investment i) {
        if (i.getCurrentPrice() == null || i.getQuantity() == null) {
            return 0.0;
        }
        return i.getCurrentPrice() * i.getQuantity();
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

        Object amount = doc.get("amount");
        if (amount instanceof Integer) {
            investment.setAmount(((Integer) amount).doubleValue());
        } else if (amount instanceof Double) {
            investment.setAmount((Double) amount);
        }

        Object quantity = doc.get("quantity");
        if (quantity instanceof Integer) {
            investment.setQuantity(((Integer) quantity).doubleValue());
        } else if (quantity instanceof Double) {
            investment.setQuantity((Double) quantity);
        }

        Object currentValue = doc.get("currentValue");
        if (currentValue instanceof Integer) {
            investment.setCurrentValue(((Integer) currentValue).doubleValue());
        } else if (currentValue instanceof Double) {
            investment.setCurrentValue((Double) currentValue);
        }

        investment.setPurchaseDate(doc.getDate("purchaseDate"));
        return investment;
    }
}