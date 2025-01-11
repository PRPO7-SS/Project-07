package ss.finance.rabbit;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConfig {
    private static final String QUEUE_NAME = "transactionQueue";
    private static final String HOST = "rabbitmq";
    private static final int PORT = 5672;
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";

    /**
     * Ustvari povezavo z RabbitMQ strežnikom.
     *
     * @return Povezava z RabbitMQ.
     * @throws Exception Če pride do napake pri vzpostavitvi povezave.
     */
    public static Connection createConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);

        // Debug: Izpis nastavitev povezave
        System.out.println("Connecting to RabbitMQ at " + factory.getHost() + ":" + factory.getPort());
        System.out.println("Using credentials: " + factory.getUsername());
        System.out.println("Attempting to establish connection...");

        Connection connection = factory.newConnection();

        System.out.println("Connection established successfully!");

        return connection;
    }

    /**
     * Vrne ime vrste sporočil.
     *
     * @return Ime vrste sporočil.
     */
    public static String getQueueName() {
        System.out.println("Returning queue name: " + QUEUE_NAME);
        return QUEUE_NAME;
    }
}