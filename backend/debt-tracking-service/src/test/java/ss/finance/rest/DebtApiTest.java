package ss.finance.rest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ss.finance.entities.Debt;
import ss.finance.security.JwtUtil;
import ss.finance.services.DebtBean;

@ExtendWith(MockitoExtension.class)
class DebtApiTest {

    @Mock
    private DebtBean debtBean;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private DebtApi debtApi;

    private String validToken;
    private ObjectId userId;
    private Debt sampleDebt;

    @BeforeEach
    void setUp() {
        userId = new ObjectId();
        validToken = "valid.jwt.token";
        sampleDebt = new Debt(userId, "Bank A", "Car Loan", 5000.00, false, new Date());

        lenient().when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
    }

    @Test
    void testGetDebts_Success() {
        List<Debt> debts = Arrays.asList(sampleDebt, new Debt(userId, "Bank B", "Mortgage", 200000.00, false, new Date()));
        when(debtBean.getDebtsByUserId(userId)).thenReturn(debts);

        Response response = debtApi.getDebts(validToken);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(debts, response.getEntity());

        verify(debtBean, times(1)).getDebtsByUserId(userId);
    }

    @Test
    void testGetDebts_NoContent() {
        when(debtBean.getDebtsByUserId(userId)).thenReturn(List.of());

        Response response = debtApi.getDebts(validToken);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        verify(debtBean, times(1)).getDebtsByUserId(userId);
    }

    @Test
    void testGetDebtById_Success() {
        ObjectId debtId = new ObjectId();
        when(debtBean.getDebtById(debtId)).thenReturn(sampleDebt);

        Response response = debtApi.getDebtById(debtId.toHexString(), validToken);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(sampleDebt, response.getEntity());

        verify(debtBean, times(1)).getDebtById(debtId);
    }

    @Test
    void testGetDebtById_NotFound() {
        ObjectId debtId = new ObjectId();
        when(debtBean.getDebtById(debtId)).thenReturn(null);

        Response response = debtApi.getDebtById(debtId.toHexString(), validToken);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Debt not found or does not belong to the user\"}", response.getEntity());

        verify(debtBean, times(1)).getDebtById(debtId);
    }

    @Test
    void testUpdateDebt_Success() {
        ObjectId debtId = new ObjectId();
        Debt sampleDebt = new Debt(userId, "Creditor", "Description", 100.0, false, new Date());
        String validToken = "valid.jwt.token"; // Simuliran veljaven JWT token
    
        // Simuliraj, da metoda `getDebtById` vrne obstoječ dolg
        when(debtBean.getDebtById(debtId)).thenReturn(sampleDebt);
    
        // Simuliraj posodobitev dolga (ker metoda vrača `boolean`, uporabimo `thenReturn(true)`)
        when(debtBean.updateDebt(any(ObjectId.class), any(Debt.class))).thenReturn(true);
    
        // Kličemo `updateDebt` z dodanim `validToken`
        Response response = debtApi.updateDebt(debtId.toHexString(), sampleDebt, validToken);
    
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Debt updated successfully\"}", response.getEntity());
    
        // Preverimo, da je metoda klicana pravilno
        verify(debtBean, times(1)).updateDebt(eq(debtId), any(Debt.class));
    }

    @Test
    void testDeleteDebt_Success() {
        ObjectId debtId = new ObjectId();
        when(debtBean.deleteDebt(debtId)).thenReturn(true);

        Response response = debtApi.deleteDebt(debtId.toHexString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Debt deleted successfully\"}", response.getEntity());

        verify(debtBean, times(1)).deleteDebt(debtId);
    }

    @Test
    void testDeleteDebt_NotFound() {
        ObjectId debtId = new ObjectId();
        when(debtBean.deleteDebt(debtId)).thenReturn(false);

        Response response = debtApi.deleteDebt(debtId.toHexString());

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Debt not found\"}", response.getEntity());

        verify(debtBean, times(1)).deleteDebt(debtId);
    }

    @Test
    void testMarkDebtAsPaid_Success() {
        ObjectId debtId = new ObjectId();
        ObjectId userId = new ObjectId();
        Debt sampleDebt = new Debt(userId, "Loan", "Test description", 100.00, false, new Date());

        // Preveri, da metoda `getDebtById` vrača pravi dolg
        when(debtBean.getDebtById(debtId)).thenReturn(sampleDebt);

        // Uporabi `doAnswer()` za void metodo `markAsPaid`
        doAnswer(invocation -> null).when(debtBean).markAsPaid(debtId);

        Response response = debtApi.markDebtAsPaid(debtId.toHexString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Debt marked as paid successfully\"}", response.getEntity());

        verify(debtBean, times(1)).getDebtById(debtId);
        verify(debtBean, times(1)).markAsPaid(debtId);
    }
}