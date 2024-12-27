package ss.finance.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.types.ObjectId;

import javax.enterprise.context.ApplicationScoped;
import javax.annotation.PostConstruct;
import io.jsonwebtoken.ExpiredJwtException;
import ss.finance.environment.Config;


@ApplicationScoped
public class JwtUtil {

    private String secretKey = Config.getEnv("JWT_SECRET");
    private String refreshSecretKey = Config.getEnv("REFRESH_TOKEN_SECRET");
    private static final long EXPIRATION_TIME = 3600000; // Default to 1h
    private static final long REFRESH_EXPIRATION_TIME = 86400000; // Default to 1day


    @PostConstruct
    public void init() {
        try {
            if (secretKey == null || secretKey.isEmpty() || refreshSecretKey == null || refreshSecretKey.isEmpty()) {
                throw new IllegalStateException("JWT_SECRET or REFRESH_TOKEN_SECRET is not set in the environment variables");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize JwtUtil: " + e.getMessage(), e);
        }
    }

    public String generateToken(ObjectId userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toHexString());  // Store the userId as a string
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String generateRefreshToken(ObjectId userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toHexString());
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME)) // 1 day validity
                .signWith(SignatureAlgorithm.HS256, refreshSecretKey) // Use a different secret key
                .compact();
    }


    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token has expired", e);
        } catch (io.jsonwebtoken.SignatureException e) {
            throw new RuntimeException("Invalid token signature", e);
        } catch (Exception e) {
            throw new RuntimeException("Error extracting claims", e);
        }
    }



    public ObjectId extractUserId(String token) {
        Claims claims = extractClaims(token);
        String userIdString = claims.get("userId", String.class);  // Extract the userId as String

        // Convert the String userId to an ObjectId
        return new ObjectId(userIdString);  // Assuming userIdString is a valid ObjectId string
    }
}

