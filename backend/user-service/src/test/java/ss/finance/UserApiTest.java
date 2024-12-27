/*import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ss.finance.entities.User;
import ss.finance.services.UserBean;
import ss.finance.security.JwtUtil;
import ss.finance.rest.UserApi;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserApiTest {

    @InjectMocks
    private UserApi userApi;

    @Mock
    private UserBean userBean;

    @Mock
    private JwtUtil jwtUtil; // Mock JwtUtil instead of directly using it.

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetUserProfile_ValidToken() {
        // Arrange
        String userId = "507f1f77bcf86cd799439011";
        String email = "test@example.com";
        String token = "valid-token";

        User mockUser = new User();
        mockUser.setId(new org.bson.types.ObjectId(userId));
        mockUser.setEmail(email);

        // Mock JWT behavior
        when(jwtUtil.extractUserId("valid-token")).thenReturn(userId);
        when(userBean.getUserById(userId)).thenReturn(mockUser);

        // Act
        Response response = userApi.getUserProfile("Bearer " + token);

        // Assert
        assertEquals(200, response.getStatus(), "Response status should be 200");
        User returnedUser = (User) response.getEntity();
        assertNotNull(returnedUser, "Returned user should not be null");
        assertEquals(email, returnedUser.getEmail(), "Email should match");
    }

    @Test
    public void testGetUserProfile_UserNotFound() {
        // Arrange
        String userId = "507f1f77bcf86cd799439011";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MzUzMDI5NTEsInVzZXJJZCI6IjY3NmQ0ZGE3YTU0ZGFmMzUzNDA4MzA3MyIsImlhdCI6MTczNTIxNjU1MSwiZW1haWwiOiJ0ZXN0M0BleGFtcGxlLmNvbSJ9.o_tLI9JVDssIiFl04I7mHmiMZiKkLrxDaw1csFmViaA";

        // Mock JWT behavior
        when(jwtUtil.extractUserId("token")).thenReturn(userId);
        when(userBean.getUserById(userId)).thenReturn(null);

        // Act
        Response response = userApi.getUserProfile("Bearer " + token);

        // Assert
        assertEquals(404, response.getStatus(), "Response status should be 404");
    }


    @Test
    public void testGetUserProfile_InvalidToken() {
        // Arrange
        String token = "invalid-token";

        when(jwtUtil.extractUserId(token)).thenThrow(new IllegalArgumentException("Invalid token"));

        // Act
        Response response = userApi.getUserProfile("Bearer " + token);

        // Assert
        assertEquals(401, response.getStatus(), "Response status should be 401");
    }

}*/
