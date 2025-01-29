package ss.finance.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import ss.finance.entities.SavingsGoal;

@ExtendWith(MockitoExtension.class)
class GoalBeanTest {

    @Mock
    private MongoCollection<Document> collection; // Mock zbirke

    @Mock
    private FindIterable<Document> findIterable; // Uporabi FindIterable namesto cursorja

    @Mock
    private MongoCursor<Document> cursor; // Mock iteratorja

    @InjectMocks
    private GoalBean goalBean; // Testirana komponenta

    private ObjectId goalId;
    private ObjectId userId;
    private SavingsGoal sampleGoal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = new ObjectId();
        goalId = new ObjectId();

        sampleGoal = new SavingsGoal();
        sampleGoal.setId(goalId.toHexString());
        sampleGoal.setUserId(userId);
        sampleGoal.setGoalName("Vacation");
        sampleGoal.setTargetAmount(5000);
        sampleGoal.setCurrentAmount(1000);
        sampleGoal.setStartDate(new Date());
        sampleGoal.setDeadline(new Date());
    }

    @Test
    void testAddSavingsGoal() {
        doNothing().when(collection).insertOne(any(Document.class));

        goalBean.addSavingsGoal(sampleGoal);

        verify(collection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testUpdateSavingsGoal() {
        doNothing().when(collection).updateOne(any(Document.class), any(Document.class));

        goalBean.updateSavingsGoal(goalId, sampleGoal);

        verify(collection, times(1)).updateOne(any(Document.class), any(Document.class));
    }

    @Test
    void testGetSavingsGoal() {
        Document goalDoc = new Document("_id", goalId)
                .append("userId", userId)
                .append("goalName", "Vacation")
                .append("targetAmount", 5000)
                .append("currentAmount", 1000)
                .append("startDate", new Date())
                .append("deadline", new Date());

        // Poskrbi, da `find` vrne `FindIterable`, ki nato vrne `cursor`
        when(collection.find(any(Document.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(goalDoc); // `first()` deluje na `FindIterable`

        SavingsGoal retrievedGoal = goalBean.getSavingsGoal(goalId);

        assertNotNull(retrievedGoal);
        assertEquals("Vacation", retrievedGoal.getGoalName());
    }

    @Test
    void testGetAllSavingsGoals() {
        Document goalDoc = new Document("_id", goalId)
                .append("userId", userId)
                .append("goalName", "Vacation")
                .append("targetAmount", 5000)
                .append("currentAmount", 1000)
                .append("startDate", new Date())
                .append("deadline", new Date());

        List<Document> goalDocs = new ArrayList<>();
        goalDocs.add(goalDoc);

        // Pravilna simulacija `find()` klica
        when(collection.find(any(Document.class))).thenReturn(findIterable);
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, false); // Ima en element
        when(cursor.next()).thenReturn(goalDoc);

        List<SavingsGoal> goals = goalBean.getAllSavingsGoals(userId);

        assertNotNull(goals);
        assertFalse(goals.isEmpty());
        assertEquals(1, goals.size());
    }

    @Test
    void testDeleteSavingsGoal() {
        doNothing().when(collection).deleteOne(any(Document.class));

        boolean result = goalBean.deleteSavingsGoal(goalId);

        assertTrue(result);
        verify(collection, times(1)).deleteOne(any(Document.class));
    }
}