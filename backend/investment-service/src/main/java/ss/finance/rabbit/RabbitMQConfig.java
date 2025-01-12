package ss.finance.rabbit;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConfig {

    private static final String QUEUE_NAME = "transactionQueue";

    /**
     * Ustvari povezavo z RabbitMQ strežnikom.
     *
     * @return Povezava z RabbitMQ.
     * @throws Exception Če pride do napake pri vzpostavitvi povezave.
     */

    public static Connection createConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        // Branje konfiguracije iz okolja (če ni nastavljena, se uporabi privzeta vrednost)
        String host = System.getenv("RABBITMQ_HOST");
        String username = System.getenv("RABBITMQ_USER");
        String password = System.getenv("RABBITMQ_PASS");
        String portStr = System.getenv("RABBITMQ_PORT");

        if (host == null || username == null || password == null || portStr == null) {
            throw new IllegalArgumentException("Missing required RabbitMQ environment variables (RABBITMQ_HOST, RABBITMQ_USER, RABBITMQ_PASS, RABBITMQ_PORT)");
        }

        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setPort(Integer.parseInt(portStr));

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