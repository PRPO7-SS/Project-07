package ss.finance.rest;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ss.finance.entities.Transaction;
import ss.finance.security.JwtUtil;
import ss.finance.services.TransactionBean;
import ss.finance.services.TransactionDTO;

@ExtendWith(MockitoExtension.class)
public class TransactionApiTest {

    @InjectMocks
    private TransactionApi transactionApi;

    @Mock
    private TransactionBean transactionBean;

    @Mock
    private JwtUtil jwtUtil;

    private String validToken;
    private ObjectId userId;
    private Transaction transaction;
    private TransactionDTO transactionDTO;

    @BeforeEach
    void setUp() {
        validToken = "valid.jwt.token";
        userId = new ObjectId();
        transaction = new Transaction(userId, "expense", 50.0, "Groceries", new Date());
        transactionDTO = new TransactionDTO();
        transactionDTO.setType("expense");
        transactionDTO.setAmount(50.0);
        transactionDTO.setCategory("Groceries");
        transactionDTO.setDate(new Date());
    }

    @Test
    void testAddTransaction_Success() {
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        doNothing().when(transactionBean).addTransaction(any(Transaction.class));

        Response response = transactionApi.addTransaction(transactionDTO, validToken);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Transaction created successfully\"}", response.getEntity());
        verify(transactionBean, times(1)).addTransaction(any(Transaction.class));
    }

    @Test
    void testAddTransaction_Unauthorized() {
        Response response = transactionApi.addTransaction(transactionDTO, null);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Token is missing or invalid\"}", response.getEntity());
        verify(transactionBean, never()).addTransaction(any(Transaction.class));
    }

    @Test
    void testGetUserTransactions_Success() {
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(transactionBean.getTransactionsByUserId(userId)).thenReturn(List.of(transaction));

        Response response = transactionApi.getUserTransactions(validToken);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(transactionBean, times(1)).getTransactionsByUserId(userId);
    }

    @Test
    void testGetUserTransactions_NoContent() {
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(transactionBean.getTransactionsByUserId(userId)).thenReturn(Collections.emptyList());

        Response response = transactionApi.getUserTransactions(validToken);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    void testGetUserTransactions_Unauthorized() {
        Response response = transactionApi.getUserTransactions(null);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Token is missing or invalid\"}", response.getEntity());
        verify(transactionBean, never()).getTransactionsByUserId(any());
    }

    @Test
    void testDeleteTransaction_Success() {
        ObjectId transactionId = new ObjectId();

        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(transactionBean.getTransactionById(transactionId)).thenReturn(transaction);
        when(transactionBean.deleteTransaction(transactionId)).thenReturn(true);

        Response response = transactionApi.deleteTransaction(transactionId.toHexString(), validToken);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(transactionBean, times(1)).deleteTransaction(transactionId);
    }

    @Test
    void testDeleteTransaction_NotFound() {
        ObjectId transactionId = new ObjectId();

        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(transactionBean.getTransactionById(transactionId)).thenReturn(null);

        Response response = transactionApi.deleteTransaction(transactionId.toHexString(), validToken);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Transaction not found or does not belong to the user\"}", response.getEntity());
        verify(transactionBean, never()).deleteTransaction(transactionId);
    }

    @Test
    void testDeleteTransaction_InvalidIdFormat() {
        Response response = transactionApi.deleteTransaction("invalid_id", validToken);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Invalid transaction ID format\"}", response.getEntity());
        verify(transactionBean, never()).deleteTransaction(any());
    }

    @Test
    void testDeleteTransaction_Unauthorized() {
        Response response = transactionApi.deleteTransaction(new ObjectId().toHexString(), null);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Token is missing or invalid\"}", response.getEntity());
        verify(transactionBean, never()).deleteTransaction(any());
    }
}