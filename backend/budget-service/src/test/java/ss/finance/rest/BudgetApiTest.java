package ss.finance.rest;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ss.finance.entities.Budget;
import ss.finance.security.JwtUtil;
import ss.finance.services.BudgetBean;
import ss.finance.services.BudgetUpdateRequest;

@ExtendWith(MockitoExtension.class)
class BudgetApiTest {

    @Mock
    private BudgetBean budgetBean;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private BudgetApi budgetApi;

    private String validToken;
    private ObjectId userId;
    private Budget sampleBudget;

    @BeforeEach
    void setUp() {
        userId = new ObjectId();
        validToken = "valid.jwt.token";
        sampleBudget = new Budget(userId, "Food", 500.00);

        // Uporabi lenient(), da preprečiš UnnecessaryStubbingException
        lenient().when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
    }

    @Test
    void testAddBudget_Success() {
        doNothing().when(budgetBean).addBudget(any(Budget.class));

        Response response = budgetApi.addBudget(sampleBudget, validToken);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Budget added successfully\"}", response.getEntity());

        verify(budgetBean, times(1)).addBudget(any(Budget.class));
    }

    @Test
    void testAddBudget_Unauthorized() {
        Response response = budgetApi.addBudget(sampleBudget, null);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Token is missing or invalid\"}", response.getEntity());

        verify(budgetBean, never()).addBudget(any(Budget.class));
    }

    @Test
    void testGetBudgets_Success() {
        List<Budget> budgets = Arrays.asList(sampleBudget, new Budget(userId, "Transport", 200.00));
        when(budgetBean.getBudgetsByUserId(userId)).thenReturn(budgets);

        Response response = budgetApi.getBudgets(validToken);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(budgets, response.getEntity());

        verify(budgetBean, times(1)).getBudgetsByUserId(userId);
    }

    @Test
    void testGetBudgets_NoContent() {
        when(budgetBean.getBudgetsByUserId(userId)).thenReturn(List.of());

        Response response = budgetApi.getBudgets(validToken);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        verify(budgetBean, times(1)).getBudgetsByUserId(userId);
    }

    @Test
    void testUpdateBudget_Success() {
        BudgetUpdateRequest request = new BudgetUpdateRequest();
        request.setNewLimit(600.00);
        doNothing().when(budgetBean).updateBudget(userId, "Food", request.getNewLimit());

        Response response = budgetApi.updateBudget("food", validToken, request);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Budget updated successfully\"}", response.getEntity());

        verify(budgetBean, times(1)).updateBudget(userId, "Food", request.getNewLimit());
    }

    @Test
    void testDeleteBudget_Success() {
        when(budgetBean.deleteBudget(userId, "Food")).thenReturn(true);

        Response response = budgetApi.deleteBudget("Food", validToken);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Budget deleted successfully\"}", response.getEntity());

        verify(budgetBean, times(1)).deleteBudget(userId, "Food");
    }

    @Test
    void testDeleteBudget_NotFound() {
        when(budgetBean.deleteBudget(userId, "Food")).thenReturn(false);

        Response response = budgetApi.deleteBudget("Food", validToken);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("{\"message\": \"Budget not found for category: Food\"}", response.getEntity());

        verify(budgetBean, times(1)).deleteBudget(userId, "Food");
    }
}