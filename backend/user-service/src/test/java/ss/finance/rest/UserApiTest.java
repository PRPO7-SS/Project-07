package ss.finance.rest;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ss.finance.entities.User;
import ss.finance.security.JwtUtil;
import ss.finance.services.UserBean;

@ExtendWith(MockitoExtension.class)
public class UserApiTest {

    @InjectMocks
    private UserApi userApi;

    @Mock
    private UserBean userBean;

    @Mock
    private JwtUtil jwtUtil;

    private String validToken;
    private ObjectId userId;
    private User user;

    @BeforeEach
    void setUp() {
        validToken = "valid.jwt.token";
        userId = new ObjectId();
        user = new User();
        user.setId(userId.toHexString());
        user.setEmail("test@example.com");
        user.setPassword("$2a$10$hashedPassword"); // Simulirana bcrypt geslo
    }

    @Test
    void testGetAllUsers_Success() {
        when(userBean.getAllUsers()).thenReturn(List.of(user));

        Response response = userApi.getAllUsers();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(userBean, times(1)).getAllUsers();
    }

    @Test
    void testGetAllUsers_ServerError() {
        when(userBean.getAllUsers()).thenThrow(new RuntimeException("Database error"));

        Response response = userApi.getAllUsers();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "Server error"), response.getEntity());
    }

    @Test
    void testChangePassword_MissingToken() {
        Response response = userApi.changePassword(null, Map.of(
                "oldPassword", "current-password",
                "newPassword", "new-password",
                "confirmNewPassword", "new-password"
        ));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "Cookie is missing or invalid"), response.getEntity());
    }

    @Test
    void testDeleteUser_Success() {
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(userBean.deleteUser(userId)).thenReturn(true);

        Response response = userApi.deleteUser(validToken);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(userBean, times(1)).deleteUser(userId);
    }

    @Test
    void testDeleteUser_NotFound() {
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(userBean.deleteUser(userId)).thenReturn(false);

        Response response = userApi.deleteUser(validToken);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "User not found"), response.getEntity());
    }

    @Test
    void testDeleteUser_Unauthorized() {
        Response response = userApi.deleteUser(null);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "Cookie is missing or invalid"), response.getEntity());
        verify(userBean, never()).deleteUser(any());
    }

    @Test
    void testGetUserProfile_Success() {
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(userBean.getUserById(userId)).thenReturn(user);

        Response response = userApi.getUserProfile(validToken);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(user, response.getEntity());
    }

    @Test
    void testGetUserProfile_NotFound() {
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(userBean.getUserById(userId)).thenReturn(null);

        Response response = userApi.getUserProfile(validToken);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "User not found"), response.getEntity());
    }

    @Test
    void testGetUserProfile_Unauthorized() {
        Response response = userApi.getUserProfile(null);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "Token is missing or invalid"), response.getEntity());
    }

    @Test
    void testUpdateUser_Success() {
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(userBean.getUserById(userId)).thenReturn(user);
        doNothing().when(userBean).updateUser(eq(userId), any(User.class));

        Response response = userApi.updateUser(validToken, user);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(Map.of("message", "User updated successfully"), response.getEntity());
    }

    @Test
    void testUpdateUser_NotFound() {
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(userBean.getUserById(userId)).thenReturn(null);

        Response response = userApi.updateUser(validToken, user);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "User not found"), response.getEntity());
    }

    @Test
    void testUpdateUser_Unauthorized() {
        Response response = userApi.updateUser(null, user);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "Cookie is missing or invalid"), response.getEntity());
    }
}