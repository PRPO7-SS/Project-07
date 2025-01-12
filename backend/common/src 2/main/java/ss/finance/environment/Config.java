package ss.finance.environment;
import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.configure().directory("../backend").load();

    public static String getEnv(String key) {
        return dotenv.get(key);
    }
}
