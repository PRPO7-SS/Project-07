package ss.finance.repositories;

import org.bson.Document;
import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

import ss.finance.entities.Budget;

class BudgetRepositoryTest {

    @Mock
    private MongoCollection<Document> mockCollection;

    @Mock
    private MongoCursor<Document> mockCursor;

    @InjectMocks
    private BudgetRepository budgetRepository;

    private ObjectId userId;
    private ObjectId budgetId;
    private Budget testBudget;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = new ObjectId();
        budgetId = new ObjectId();
        testBudget = new Budget(userId, "Food", 500.00);
    }

    @Test
    void testAddBudget() {
        Budget budget = new Budget(userId, "Food", 500.0);

        when(mockCollection.insertOne(any(Document.class))).thenReturn(mock(InsertOneResult.class));

        budgetRepository.addBudget(budget);

        verify(mockCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testGetBudgetByUserIdAndCategory_Found() {
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
    
        when(mockCollection.find(any(Document.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(new Document()
                .append("_id", budgetId)
                .append("userId", userId)
                .append("category", "Food")
                .append("monthlyLimit", 500.00));
    
        Budget budget = budgetRepository.getBudgetByUserIdAndCategory(userId, "Food");
    
        assertNotNull(budget);
        assertEquals("Food", budget.getCategory());
        assertEquals(500.00, budget.getMonthlyLimit());
    }

    @Test
    void testGetBudgetByUserIdAndCategory_NotFound() {
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
    
        when(mockCollection.find(any(Document.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null); // Simuliramo, da ni zadetkov
    
        Budget budget = budgetRepository.getBudgetByUserIdAndCategory(userId, "NonExistingCategory");
    
        assertNull(budget);
    }

    @Test
    void testUpdateMonthlyLimit_Success() {
        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getMatchedCount()).thenReturn(1L);
        when(mockCollection.updateOne(any(Document.class), any(Document.class))).thenReturn(updateResult);

        budgetRepository.updateMonthlyLimit(budgetId, 600.00);

        verify(mockCollection, times(1)).updateOne(any(Document.class), any(Document.class));
    }

    @Test
    void testUpdateMonthlyLimit_NotFound() {
        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getMatchedCount()).thenReturn(0L);
        when(mockCollection.updateOne(any(Document.class), any(Document.class))).thenReturn(updateResult);

        budgetRepository.updateMonthlyLimit(new ObjectId(), 600.00);

        verify(mockCollection, times(1)).updateOne(any(Document.class), any(Document.class));
    }

    @Test
    void testDeleteBudget_Success() {
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(1L);
        when(mockCollection.deleteOne(any(Document.class))).thenReturn(deleteResult);

        boolean deleted = budgetRepository.deleteBudget(userId, "Food");

        assertTrue(deleted);
        verify(mockCollection, times(1)).deleteOne(any(Document.class));
    }

    @Test
    void testDeleteBudget_NotFound() {
        DeleteResult deleteResult = mock(DeleteResult.class);
        when(deleteResult.getDeletedCount()).thenReturn(0L);
        when(mockCollection.deleteOne(any(Document.class))).thenReturn(deleteResult);

        boolean deleted = budgetRepository.deleteBudget(userId, "NonExistentCategory");

        assertFalse(deleted);
        verify(mockCollection, times(1)).deleteOne(any(Document.class));
    }
}