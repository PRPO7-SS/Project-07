package ss.finance.services;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ss.finance.entities.User;
import ss.finance.utils.MongoDBConnection;

@ExtendWith(MockitoExtension.class)
public class UserBeanTest {

    @Mock
    private MongoClient mockMongoClient;

    @Mock
    private MongoDatabase mockDatabase;

    @Mock
    private MongoCollection<Document> mockCollection;

    @Mock
    private FindIterable<Document> mockFindIterable;

    @InjectMocks
    private UserBean userBean;

    @BeforeEach
    void setUp() {
        lenient().when(mockMongoClient.getDatabase("financeApp")).thenReturn(mockDatabase);
        lenient().when(mockDatabase.getCollection("users")).thenReturn(mockCollection);
        lenient().when(mockCollection.find(any(Document.class))).thenReturn(mockFindIterable);
        MongoDBConnection.setTestMongoClient(mockMongoClient);
    }

    @Test
    void testAddUser_Success() {
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        when(mockFindIterable.first()).thenReturn(null);

        userBean.addUser(user);

        verify(mockCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testAddUser_UserAlreadyExists() {
        when(mockFindIterable.first()).thenReturn(new Document("email", "test@example.com"));

        User user = new User();
        user.setEmail("test@example.com");

        userBean.addUser(user);

        verify(mockCollection, never()).insertOne(any(Document.class));
    }

    @Test
    void testExistingUser_UserFound() {
        when(mockFindIterable.first()).thenReturn(new Document("email", "test@example.com"));

        boolean exists = userBean.existingUser("test@example.com");

        assertEquals(true, exists);
    }

    @Test
    void testExistingUser_UserNotFound() {
        when(mockFindIterable.first()).thenReturn(null);

        boolean exists = userBean.existingUser("nonexistent@example.com");

        assertEquals(false, exists);
    }

    @Test
    void testValidateUser_CorrectPassword() {
        String plainPassword = "password";
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        Document userDoc = new Document("email", "test@example.com")
                               .append("password", hashedPassword)
                               .append("balance", 100.0)
                               .append("age", 25)
                               .append("someField", 10.5);

        when(mockCollection.find(any(Document.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(userDoc);

        User user = userBean.validateUser("test@example.com", plainPassword);

        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void testValidateUser_WrongPassword() {
        String hashedPassword = BCrypt.hashpw("password", BCrypt.gensalt());
        Document userDoc = new Document("email", "test@example.com").append("password", hashedPassword);

        when(mockFindIterable.first()).thenReturn(userDoc);

        User user = userBean.validateUser("test@example.com", "wrongpassword");

        assertEquals(null, user);
    }

    @Test
    void testDeleteUser_Success() {
        ObjectId userId = new ObjectId();
        Document userDoc = new Document("_id", userId);

        when(mockFindIterable.first()).thenReturn(userDoc);

        boolean deleted = userBean.deleteUser(userId);

        assertEquals(true, deleted);
        verify(mockCollection, times(1)).deleteOne(eq(new Document("_id", userId)));
    }

    @Test
    void testDeleteUser_NotFound() {
        ObjectId userId = new ObjectId();

        when(mockFindIterable.first()).thenReturn(null);

        boolean deleted = userBean.deleteUser(userId);

        assertEquals(false, deleted);
        verify(mockCollection, never()).deleteOne(any(Document.class));
    }

    @Test
    void testUpdateUser_Success() {
        ObjectId userId = new ObjectId();
        Document existingUser = new Document("_id", userId)
                .append("email", "old@example.com")
                .append("password", "oldpassword");
    
        when(mockFindIterable.first()).thenReturn(existingUser); // Simulacija obstoječega uporabnika
    
        User updatedUser = new User();
        updatedUser.setEmail("test@example.com");
        updatedUser.setPassword("newpassword");
    
        userBean.updateUser(userId, updatedUser);
    
        verify(mockCollection, times(1)).updateOne(any(Document.class), any(Document.class));
    }

    @Test
    void testUpdateUser_NotFound() {
        ObjectId userId = new ObjectId();
    
        when(mockFindIterable.first()).thenReturn(null); // Simuliraj neobstoj uporabnika
    
        User updatedUser = new User();
        updatedUser.setEmail("test@example.com");
        updatedUser.setPassword("newpassword");
    
        userBean.updateUser(userId, updatedUser);
    
        verify(mockCollection, never()).updateOne(any(Document.class), any(Document.class));
    }

    @Test
    void testGetUserByEmail_Success() {
        Document userDoc = new Document("email", "test@example.com").append("password", "hashedpassword");
        when(mockFindIterable.first()).thenReturn(userDoc);

        User user = userBean.getUserByEmail("test@example.com");

        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void testGetUserByEmail_NotFound() {
        when(mockFindIterable.first()).thenReturn(null);

        User user = userBean.getUserByEmail("nonexistent@example.com");

        assertNull(user);
    }
    
    @Test
    void testAddUser_InvalidEmail() {
        User user = new User();
        user.setEmail("invalid-email"); // Neveljaven email
    
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userBean.addUser(user);
        });
    
        assertTrue(exception.getMessage().contains("Invalid email format."));
    }

    @Test
    void testValidateUser_NullEmail() {
        User user = userBean.validateUser(null, "password");
        assertNull(user);
    }

    @Test
    void testValidateUser_NullPassword() {
        User user = userBean.validateUser("test@example.com", null);
        assertNull(user);
    }

    @Test
    void testValidateUser_EmptyPassword() {
        User user = userBean.validateUser("test@example.com", "");
        assertNull(user);
    }
}
