package ss.finance.rest;

import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ss.finance.entities.User;
import ss.finance.security.JwtUtil;
import ss.finance.services.UserBean;

@ExtendWith(MockitoExtension.class)
class AuthApiTest {

    @InjectMocks
    private AuthApi authApi;

    @Mock
    private UserBean userBean;

    @Mock
    private JwtUtil jwtUtil;

    private ObjectId userId;
    private User user;
    private String hashedPassword;
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        userId = new ObjectId();
        hashedPassword = BCrypt.hashpw("securepassword", BCrypt.gensalt());
        accessToken = "mockAccessToken";
        refreshToken = "mockRefreshToken";

        user = new User();
        user.setId(userId.toHexString());  // Shranimo kot String
        user.setEmail("test@example.com");
        user.setPassword(hashedPassword);

        lenient().when(userBean.getUserById(userId)).thenReturn(user);
        lenient().when(userBean.getUserId(user.getEmail())).thenReturn(userId);
        lenient().when(jwtUtil.generateToken(userId, user.getEmail())).thenReturn(accessToken);
        lenient().when(jwtUtil.generateRefreshToken(userId, user.getEmail())).thenReturn(refreshToken);
    }

    @Test
    void testRegisterUser_Success() {
        User newUser = new User();
        newUser.setFullName("Test User");
        newUser.setEmail("test@example.com");
        newUser.setUsername("testuser");
        newUser.setPassword("securepassword");

        when(userBean.existingUser(newUser.getEmail())).thenReturn(false);

        Response response = authApi.addUser(newUser);
        assertEquals(201, response.getStatus());
        verify(userBean, times(1)).addUser(any(User.class));
    }

    @Test
    void testLoginUser_Success() {
        User loginUser = new User();
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("securepassword");

        when(userBean.validateUser(loginUser.getEmail(), loginUser.getPassword())).thenReturn(user);

        Response response = authApi.loginUser(loginUser);
        assertEquals(200, response.getStatus());
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        User invalidUser = new User();
        invalidUser.setEmail("test@example.com");
        invalidUser.setPassword("wrongpassword");

        when(userBean.validateUser(invalidUser.getEmail(), invalidUser.getPassword())).thenReturn(null);

        Response response = authApi.loginUser(invalidUser);
        assertEquals(401, response.getStatus());
    }

    @Test
    void testRefreshToken_Success() {
        when(jwtUtil.extractRefreshUserId(refreshToken)).thenReturn(userId); // 🔥 ObjectId, ne String

        Response response = authApi.refreshToken(refreshToken);
        assertEquals(200, response.getStatus());
        verify(jwtUtil, times(1)).generateToken(userId, user.getEmail()); // 🔥 ObjectId
    }

    @Test
    void testRefreshToken_InvalidToken() {
        when(jwtUtil.extractRefreshUserId(refreshToken)).thenThrow(new RuntimeException("Invalid token"));

        Response response = authApi.refreshToken(refreshToken);
        assertEquals(500, response.getStatus());
    }

    @Test
    void testLogoutUser_Success() {
        Response response = authApi.logOut();
        assertEquals(200, response.getStatus());
    }
}