package ss.finance.services;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ss.finance.entities.Budget;
import ss.finance.repositories.BudgetRepository;

@ExtendWith(MockitoExtension.class)
class BudgetBeanTest {

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetBean budgetBean;

    private ObjectId userId;
    private Budget sampleBudget;

    @BeforeEach
    void setUp() {
        userId = new ObjectId();
        sampleBudget = new Budget(userId, "Food", 500.00);
    }

    @Test
    void testAddBudget() {
        doNothing().when(budgetRepository).addBudget(sampleBudget);

        budgetBean.addBudget(sampleBudget);

        verify(budgetRepository, times(1)).addBudget(sampleBudget);
    }

    @Test
    void testGetBudgetsByUserId() {
        List<Budget> budgets = Arrays.asList(sampleBudget, new Budget(userId, "Transport", 200.00));
        when(budgetRepository.getBudgetsByUserId(userId)).thenReturn(budgets);

        List<Budget> result = budgetBean.getBudgetsByUserId(userId);

        assertEquals(2, result.size());
        assertEquals("Food", result.get(0).getCategory());
        assertEquals("Transport", result.get(1).getCategory());

        verify(budgetRepository, times(1)).getBudgetsByUserId(userId);
    }

    @Test
    void testUpdateBudget_Success() {
        when(budgetRepository.getBudgetByUserIdAndCategory(userId, "Food")).thenReturn(sampleBudget);
        doNothing().when(budgetRepository).updateMonthlyLimit(sampleBudget.getId(), 600.00);

        budgetBean.updateBudget(userId, "Food", 600.00);

        verify(budgetRepository, times(1)).updateMonthlyLimit(sampleBudget.getId(), 600.00);
    }

    @Test
    void testUpdateBudget_NotFound() {
        when(budgetRepository.getBudgetByUserIdAndCategory(userId, "Food")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            budgetBean.updateBudget(userId, "Food", 600.00);
        });

        assertEquals("Budget not found for the given user and category", exception.getMessage());
        verify(budgetRepository, never()).updateMonthlyLimit(any(), anyDouble());
    }

    @Test
    void testDeleteBudget_Success() {
        when(budgetRepository.deleteBudget(userId, "Food")).thenReturn(true);

        boolean result = budgetBean.deleteBudget(userId, "Food");

        assertTrue(result);
        verify(budgetRepository, times(1)).deleteBudget(userId, "Food");
    }

    @Test
    void testDeleteBudget_Failure() {
        when(budgetRepository.deleteBudget(userId, "Food")).thenReturn(false);

        boolean result = budgetBean.deleteBudget(userId, "Food");

        assertFalse(result);
        verify(budgetRepository, times(1)).deleteBudget(userId, "Food");
    }
}