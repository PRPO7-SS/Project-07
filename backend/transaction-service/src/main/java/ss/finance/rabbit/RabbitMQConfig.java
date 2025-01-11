package ss.finance.rabbit;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConfig {
    private static final String QUEUE_NAME = "transactionQueue";

    public static Connection createConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
    
        // Debug: Izpis nastavitev povezave
        System.out.println("Connecting to RabbitMQ at " + factory.getHost() + ":" + factory.getPort());
        System.out.println("Using credentials: " + factory.getUsername());
        System.out.println("Attempting to establish connection...");

        Connection connection = factory.newConnection();
    
        System.out.println("Connection established successfully!");
    
        return connection;
    }

    public static String getQueueName() {
        return QUEUE_NAME;
    }
}