package ss.finance.services;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import ss.finance.entities.Investment;

@ExtendWith(MockitoExtension.class)
class InvestmentBeanTest {

    @Mock
    private MongoCollection<Document> mockInvestmentCollection;

    @Mock
    private MongoCollection<Document> mockTransactionCollection;

    @Mock
    private MongoDatabase mockDatabase;

    @InjectMocks
    private InvestmentBean investmentBean;

    private Investment sampleInvestment;
    private ObjectId investmentId;
    private ObjectId userId;

    @BeforeEach
    void setUp() {
        investmentId = new ObjectId();
        userId = new ObjectId();
        sampleInvestment = new Investment();
        sampleInvestment.setUserId(userId);
        sampleInvestment.setType("Stocks");
        sampleInvestment.setName("Apple");
        sampleInvestment.setAmount(1000.0);
        sampleInvestment.setQuantity(10.0);
        sampleInvestment.setPurchaseDate(new Date());

        lenient().when(mockDatabase.getCollection("investments")).thenReturn(mockInvestmentCollection);
        lenient().when(mockDatabase.getCollection("transXinvst")).thenReturn(mockTransactionCollection);
    }

    @Test
    void testAddInvestment() {
        doNothing().when(mockInvestmentCollection).insertOne(any(Document.class));

        assertDoesNotThrow(() -> investmentBean.addInvestment(sampleInvestment));

        verify(mockInvestmentCollection, times(1)).insertOne(any(Document.class));
    }

    @Test
    void testGetInvestment() {
        Document mockDocument = new Document("_id", investmentId)
                .append("userId", userId)
                .append("type", "Stocks")
                .append("name", "Apple")
                .append("amount", 1000.0)
                .append("quantity", 10.0)
                .append("purchaseDate", new Date());

        when(mockInvestmentCollection.find(any(Document.class)).first()).thenReturn(mockDocument);

        Investment fetchedInvestment = investmentBean.getInvestment(investmentId);

        assertNotNull(fetchedInvestment);
        assertEquals("Stocks", fetchedInvestment.getType());
        assertEquals("Apple", fetchedInvestment.getName());
    }

    @Test
    void testUpdateInvestment() {
        UpdateResult mockResult = mock(UpdateResult.class);
        when(mockInvestmentCollection.updateOne(any(Document.class), any(Document.class))).thenReturn(mockResult);

        assertDoesNotThrow(() -> investmentBean.updateInvestment(investmentId, sampleInvestment));

        verify(mockInvestmentCollection, times(1)).updateOne(any(Document.class), any(Document.class));
    }

    @Test
    void testDeleteInvestment() {
        DeleteResult mockResult = mock(DeleteResult.class);
        when(mockInvestmentCollection.deleteOne(any(Document.class))).thenReturn(mockResult);
        when(mockResult.wasAcknowledged()).thenReturn(true);

        boolean result = investmentBean.deleteInvestment(investmentId);

        assertTrue(result);
        verify(mockInvestmentCollection, times(1)).deleteOne(any(Document.class));
    }

    @Test
    void testGetAllInvestments() {
        // Pripravimo simulirano FindIterable<Document>
        FindIterable<Document> findIterableMock = mock(FindIterable.class);
        MongoCursor<Document> cursorMock = mock(MongoCursor.class);
    
        Document mockDocument = new Document("_id", investmentId)
                .append("userId", userId)
                .append("type", "Stocks")
                .append("name", "Apple")
                .append("amount", 1000.0)
                .append("quantity", 10.0)
                .append("purchaseDate", new Date());
    
        // Konfiguriramo mock, da vrne naš mockCursor
        when(mockInvestmentCollection.find(any(Document.class))).thenReturn(findIterableMock);
        when(findIterableMock.iterator()).thenReturn(cursorMock);
        when(cursorMock.hasNext()).thenReturn(true, false); // Vrne en dokument, nato se ustavi
        when(cursorMock.next()).thenReturn(mockDocument);
    
        // Kličemo metodo, ki jo testiramo
        List<Investment> investments = investmentBean.getAllInvestments(userId);
    
        // Preverimo rezultate
        assertNotNull(investments);
        assertEquals(1, investments.size());
        assertEquals("Apple", investments.get(0).getName());
    
        // Preverimo, ali je bila klicana metoda find()
        verify(mockInvestmentCollection, times(1)).find(any(Document.class));
    }
}