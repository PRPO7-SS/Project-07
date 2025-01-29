package ss.finance.rest;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.mockito.junit.jupiter.MockitoExtension;

import ss.finance.entities.Investment;
import ss.finance.security.JwtUtil;
import ss.finance.services.InvestmentBean;

@ExtendWith(MockitoExtension.class)
class InvestmentApiTest {

    @InjectMocks
    private InvestmentApi investmentApi;

    @Mock
    private InvestmentBean investmentBean;

    @Mock
    private JwtUtil jwtUtil;

    private Investment sampleInvestment;
    private ObjectId investmentId;
    private String validToken;

    @BeforeEach
    void setUp() {
        investmentId = new ObjectId();
        validToken = "valid.jwt.token";
        sampleInvestment = new Investment(
            investmentId, "Stocks", "Apple Inc.", 1000.0, 10.0, new Date()
        );
    }

    @Test
    void testGetInvestments_Success() {
        ObjectId userId = new ObjectId();
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(investmentBean.getAllInvestments(userId)).thenReturn(List.of(sampleInvestment));

        Response response = investmentApi.getInvestments(validToken);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(investmentBean, times(1)).getAllInvestments(userId);
    }

    @Test
    void testGetInvestments_Unauthorized() {
        Response response = investmentApi.getInvestments(null);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void testGetInvestmentById_Success() {
        ObjectId userId = new ObjectId();
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(investmentBean.getInvestment(investmentId)).thenReturn(sampleInvestment);
        sampleInvestment.setUserId(userId);

        Response response = investmentApi.getInvestmentById(validToken, investmentId.toHexString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(investmentBean, times(1)).getInvestment(investmentId);
    }

    @Test
    void testGetInvestmentById_NotFound() {
        ObjectId userId = new ObjectId();
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(investmentBean.getInvestment(investmentId)).thenReturn(null);

        Response response = investmentApi.getInvestmentById(validToken, investmentId.toHexString());

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void testAddInvestment_Success() {
        ObjectId userId = new ObjectId();
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        doNothing().when(investmentBean).addInvestment(any(Investment.class));

        Response response = investmentApi.addInvestment(validToken, sampleInvestment);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(investmentBean, times(1)).addInvestment(any(Investment.class));
    }

    @Test
    void testAddInvestment_BadRequest() {
        ObjectId userId = new ObjectId();
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);

        Investment invalidInvestment = new Investment();
        Response response = investmentApi.addInvestment(validToken, invalidInvestment);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void testDeleteInvestment_Success() {
        ObjectId userId = new ObjectId();
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(investmentBean.deleteInvestment(investmentId)).thenReturn(true);

        Response response = investmentApi.deleteInvestment(validToken, investmentId.toHexString());

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(investmentBean, times(1)).deleteInvestment(investmentId);
    }

    @Test
    void testDeleteInvestment_NotFound() {
        ObjectId userId = new ObjectId();
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(investmentBean.deleteInvestment(investmentId)).thenReturn(false);

        Response response = investmentApi.deleteInvestment(validToken, investmentId.toHexString());

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void testGetLastTransaction_Success() {
        ObjectId userId = new ObjectId();
        Document lastTransaction = new Document(Map.of(
                "userId", userId.toHexString(),
                "lastTransactionAmount", 55.0,
                "lastTransactionType", "income",
                "timestamp", "2025-01-11T12:00:00Z"
        ));

        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(investmentBean.getLastTransaction(userId.toHexString())).thenReturn(lastTransaction);

        Response response = investmentApi.getLastTransaction(validToken);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(investmentBean, times(1)).getLastTransaction(userId.toHexString());
    }

    @Test
    void testGetLastTransaction_NotFound() {
        ObjectId userId = new ObjectId();
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(investmentBean.getLastTransaction(userId.toHexString())).thenReturn(null);

        Response response = investmentApi.getLastTransaction(validToken);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    void testDeleteAllTransactions_Success() {
        ObjectId userId = new ObjectId();
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(investmentBean.deleteAllTransactions(userId.toHexString())).thenReturn(true);

        Response response = investmentApi.deleteAllTransactions(validToken);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(investmentBean, times(1)).deleteAllTransactions(userId.toHexString());
    }

    @Test
    void testDeleteAllTransactions_Failure() {
        ObjectId userId = new ObjectId();
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(investmentBean.deleteAllTransactions(userId.toHexString())).thenReturn(false);

        Response response = investmentApi.deleteAllTransactions(validToken);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
}