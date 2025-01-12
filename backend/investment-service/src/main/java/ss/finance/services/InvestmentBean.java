package ss.finance.services;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private MongoCollection<Document> investmentCollection; // Zbirka za investicije
    private MongoCollection<Document> transactionCollection; // Zbirka za transakcije
    private static final Logger logger = Logger.getLogger(InvestmentBean.class.getName());
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();


    public InvestmentBean() {
        MongoClient mongoClient = MongoDBConnection.getMongoClient();
        MongoDatabase database = mongoClient.getDatabase("financeApp");
        // Inicializacija zbirk
        this.investmentCollection = database.getCollection("investments"); // Zbirka za investicije
        this.transactionCollection = database.getCollection("transXinvst"); // Zbirka za transakcije
    }

    @PostConstruct
    public void init() {
        try {
            logger.info("Delaying RabbitMQ listener initialization for 10 seconds...");
            Thread.sleep(10000); // Delay to ensure RabbitMQ is ready
    
            logger.info("Initializing RabbitMQ listener for InvestmentService...");
            
            // Zaženemo RabbitMQ consumer v ločeni niti
            executor.submit(() -> {
                try {
                    startRabbitMQConsumer();
                } catch (Exception e) {
                    logger.severe("RabbitMQ consumer initialization failed: " + e.getMessage());
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning("Initialization delay was interrupted: " + e.getMessage());
        }
    }

    public void startRabbitMQConsumer() {
        while (true) { // Zanko prestavimo sem, da lahko ob napaki znova vzpostavimo povezavo
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
                        Thread.currentThread().interrupt(); // Ponastavite zastavico prekinjene niti
                    } catch (Exception e) {
                        logger.severe("Error while processing messages: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.severe("RabbitMQ connection failed, retrying in 10 seconds: " + e.getMessage());
                try {
                    Thread.sleep(10000); // Počakaj 10 sekund pred ponovnim poskusom
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.warning("Retry delay interrupted: " + ie.getMessage());
                }
            }
        }
    }

    /* 
    private void processMessage(String message) {
        logger.info("Received a new transaction: " + message);
        logger.info("RabbitMQ messages work! 😊");
        logger.info("Thank you for using our service! 😊");
    }
    */

    private void processMessage(String message) {
        try {
            // Parsiranje prejetega sporočila
            ObjectMapper objectMapper = new ObjectMapper();
            InvestmentMessage transaction = objectMapper.readValue(message, InvestmentMessage.class);
    
            // Nastavimo trenutni čas, če timestamp ni nastavljen
            if (transaction.getTimestamp() == null) {
                transaction.setTimestamp(new Date());
            }
    
            logger.info("Processing transaction: Amount=" + transaction.getAmount() + 
                        ", Type=" + transaction.getType() + 
                        ", Timestamp=" + transaction.getTimestamp());
    
            // Shranimo transakcijo
            saveTransaction(transaction);
    
        } catch (JsonProcessingException e) {
            logger.severe("Failed to process message. Invalid JSON format: " + message);
            logger.severe("Error details: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Unexpected error while processing message: " + e.getMessage());
        }
    }

    private void saveTransaction(InvestmentMessage transaction) {
        try {
            Document document = new Document()
                    .append("userId", transaction.getUserId()) // Uporabnik, ki je poslal transakcijo
                    .append("lastTransactionAmount", transaction.getAmount()) // Znesek transakcije
                    .append("lastTransactionType", transaction.getType()) // Tip transakcije (income/expense)
                    .append("timestamp", transaction.getTimestamp()); // Čas transakcije
            
            logger.info("Attempting to save transaction: " + document.toJson());
            transactionCollection.insertOne(document); // Shranimo v zbirko MongoDB
    
            logger.info("Transaction saved: UserId=" + transaction.getUserId() +
                        ", Amount=" + transaction.getAmount() +
                        ", Type=" + transaction.getType() +
                        ", Timestamp=" + transaction.getTimestamp());
        } catch (Exception e) {
            logger.severe("Error saving transaction to database: " + e.getMessage());
        }
    }

    public boolean deleteAllTransactions(String userId) {
        try {
            logger.info("Deleting all transactions for userId: " + userId);
    
            // Brisanje vseh transakcij za določen userId
            transactionCollection.deleteMany(new Document("userId", userId));
    
            logger.info("All transactions deleted successfully for userId: " + userId);
            return true;
        } catch (Exception e) {
            logger.severe("Error deleting transactions: " + e.getMessage());
            return false;
        }
    }

    public void addInvestment(Investment investment) {
        try {
            Document investmentDoc = toDocument(investment);
            investmentCollection.insertOne(investmentDoc);
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
            investmentCollection.updateOne(
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
            Document doc = investmentCollection.find(new Document("_id", investmentId)).first();
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
            for (Document doc : investmentCollection.find(new Document("userId", userId))) {
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
            investmentCollection.deleteOne(new Document("_id", investmentId));
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

    public Document getLastTransaction(String userId) {
        try {
            return transactionCollection.find(new Document("userId", userId))
                    .sort(new Document("timestamp", -1)) // Razvrsti po datumu (najnovejša prva)
                    .first(); // Vrne prvo najnovejšo transakcijo
        } catch (Exception e) {
            logger.severe("Error retrieving last transaction: " + e.getMessage());
            return null;
        }
    }

    public List<Document> getAllTransactions(String userId) {
        logger.info("Querying database for transactions with userId: " + userId);
    
        List<Document> transactions = transactionCollection.find(new Document("userId", userId)) // userId kot String
                .sort(new Document("timestamp", -1)) // Razvrstitev po datumu (najnovejša prva)
                .into(new ArrayList<>());
    
        logger.info("Transactions found: " + transactions.size());
        return transactions;
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