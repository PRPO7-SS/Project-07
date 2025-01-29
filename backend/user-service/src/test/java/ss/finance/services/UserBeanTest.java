package ss.finance.services;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        // Simuliraj MongoDB povezavo samo, če je metoda uporabljena v testih
        lenient().when(mockMongoClient.getDatabase("financeApp")).thenReturn(mockDatabase);
        lenient().when(mockDatabase.getCollection("users")).thenReturn(mockCollection);
        lenient().when(mockCollection.find(any(Document.class))).thenReturn(mockFindIterable);

        // Nastavi testni MongoDB klient v MongoDBConnection
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

        when(mockFindIterable.first()).thenReturn(null); // User ne obstaja

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

        assert exists;
    }

    @Test
    void testExistingUser_UserNotFound() {
        when(mockFindIterable.first()).thenReturn(null);

        boolean exists = userBean.existingUser("nonexistent@example.com");

        assert !exists;
    }

    @Test
    void testValidateUser_CorrectPassword() {
        String plainPassword = "password";
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    
        Document userDoc = new Document("email", "test@example.com")
                               .append("password", hashedPassword)
                               .append("balance", 100.0)  // Preveri, če toUser() pričakuje to polje
                               .append("age", 25)         // Preveri, če obstajajo druga številska polja
                               .append("someField", 10.5); // Dodaj manjkajoče vrednosti, ki jih uporablja toUser()
    
        when(mockCollection.find(any(Document.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(userDoc);
    
        User user = userBean.validateUser("test@example.com", plainPassword);
    
        assertNotNull(user, "User should not be null when correct password is provided.");
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void testValidateUser_WrongPassword() {
        String hashedPassword = "$2a$10$7QZzOeV6BQWuoY6pkAQu/O6I5gxzUqNe6NECcI9MfbfXzVbO4e52C"; // Hash za "password"
        Document userDoc = new Document("email", "test@example.com").append("password", hashedPassword);

        when(mockFindIterable.first()).thenReturn(userDoc);

        User user = userBean.validateUser("test@example.com", "wrongpassword");

        assert user == null;
    }

    @Test
    void testDeleteUser_Success() {
        ObjectId userId = new ObjectId();
        Document userDoc = new Document("_id", userId);

        when(mockFindIterable.first()).thenReturn(userDoc);

        boolean deleted = userBean.deleteUser(userId);

        assert deleted;
        verify(mockCollection, times(1)).deleteOne(eq(new Document("_id", userId)));
    }

    @Test
    void testDeleteUser_NotFound() {
        ObjectId userId = new ObjectId();

        when(mockFindIterable.first()).thenReturn(null);

        boolean deleted = userBean.deleteUser(userId);

        assert !deleted;
        verify(mockCollection, never()).deleteOne(any(Document.class));
    }
}