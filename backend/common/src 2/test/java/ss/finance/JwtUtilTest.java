/*import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ss.finance.security.JwtUtil;
import io.jsonwebtoken.Claims;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.bson.types.ObjectId;

public class JwtUtilTest {

    private static JwtUtil jwtUtil;  // Declare jwtUtil instance

    @BeforeAll
    public static void setup() {
        System.setProperty("JWT_SECRET", "test-secret"); // Simulate .env

        // Initialize the JwtUtil instance
        jwtUtil = new JwtUtil();
        jwtUtil.init();  // Assuming init() is used to set up necessary configurations
    }

    @Test
    public void testGenerateTokenAndExtractClaims() {
        // Create an ObjectId for the user
        ObjectId userId = new ObjectId();
        String email = "test@example.com";

        // Generate the token using JwtUtil
        String token = jwtUtil.generateToken(userId, email);  // userId is passed as ObjectId, but stored as String in JWT

        // Extract the userId from the token
        ObjectId extractedUserId = jwtUtil.extractUserId(token);  // This will return ObjectId

        // Assert that the extracted ObjectId matches the original ObjectId
        assertEquals(userId, extractedUserId);  // Compare the original ObjectId with the extracted one
    }

    /*@Test
    public void testTokenExpiration() {
        // Arrange
        String userId = "12345";  // Use String as userId here for the test
        String email = "test@example.com";

        // Act
        String token = jwtUtil.generateToken(new ObjectId(userId), email);  // Assuming generateToken now works with String
        Claims claims = jwtUtil.extractClaims(token);

        // Assert
        assertNotNull(claims.getExpiration(), "Token expiration should not be null");
        assertTrue(claims.getExpiration().after(new Date()), "Expiration should be in the future");
    }
}*/
