package ss.finance.rabbit;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConfig {

    public static Connection createConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        // Debug: Izpis nastavitev povezave
        System.out.println("DEBUG: Starting connection to RabbitMQ...");
        System.out.println("DEBUG: Host: " + factory.getHost());
        System.out.println("DEBUG: Port: " + factory.getPort());
        System.out.println("DEBUG: Username: " + factory.getUsername());
        System.out.println("DEBUG: Password: " + factory.getPassword()); // Po potrebi odstranite za varnost

        try {
            Connection connection = factory.newConnection();
            System.out.println("DEBUG: RabbitMQ connection established successfully!");
            return connection;
        } catch (java.net.ConnectException ce) {
            System.err.println("ERROR: Connection refused. Is RabbitMQ running and accessible?");
            throw ce;
        } catch (Exception e) {
            System.err.println("ERROR: General error while connecting to RabbitMQ.");
            e.printStackTrace();
            throw e;
        }
    }

    public static String getQueueName() {
        String queueName = "transactionQueue";
        // Debug: Izpis imena vrste
        System.out.println("DEBUG: Returning queue name: " + queueName);
        return queueName; // Ime vrste
    }
}