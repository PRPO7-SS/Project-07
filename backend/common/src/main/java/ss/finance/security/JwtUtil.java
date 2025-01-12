package ss.finance.security;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.bson.types.ObjectId;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@ApplicationScoped
public class JwtUtil {

    private final String secretKey = System.getenv("JWT_SECRET");
    private final String refreshSecretKey = System.getenv("REFRESH_TOKEN_SECRET");
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
        claims.put("userId", userId.toHexString());
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
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
                .signWith(SignatureAlgorithm.HS256, refreshSecretKey.getBytes(StandardCharsets.UTF_8)) // Use a different secret key
                .compact();
    }


    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8)) // Use StandardCharsets.UTF_8
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


    public Claims extractRefreshClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(refreshSecretKey.getBytes(StandardCharsets.UTF_8)) // Use StandardCharsets.UTF_8
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Refresh token has expired", e);
        } catch (io.jsonwebtoken.SignatureException e) {
            throw new RuntimeException("Invalid refresh token signature", e);
        } catch (Exception e) {
            throw new RuntimeException("Error extracting claims", e);
        }
    }

    public ObjectId extractRefreshUserId(String token) {
        Claims claims = extractRefreshClaims(token);
        String userIdString = claims.get("userId", String.class);

        return new ObjectId(userIdString);
    }
}

