package ss.finance.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import ss.finance.entities.Debt;
import ss.finance.repositories.DebtRepository;

@ExtendWith(MockitoExtension.class)
class DebtBeanTest {

    @Mock
    private DebtRepository debtRepository;

    @InjectMocks
    private DebtBean debtBean;

    private ObjectId userId;
    private ObjectId debtId;
    private Debt testDebt;

    @BeforeEach
    void setUp() {
        userId = new ObjectId();
        debtId = new ObjectId();
        testDebt = new Debt(userId, "Bank A", "Car loan", 1000.00, false, new Date());
        testDebt.setId(debtId);
    }

    @Test
    void testAddDebt() {
        doNothing().when(debtRepository).addDebt(any(Debt.class));

        debtBean.addDebt(testDebt);

        verify(debtRepository, times(1)).addDebt(testDebt);
    }

    @Test
    void testGetDebtsByUserId() {
        when(debtRepository.getDebtsByUserId(userId)).thenReturn(Collections.singletonList(testDebt));

        List<Debt> debts = debtBean.getDebtsByUserId(userId);

        assertEquals(1, debts.size());
        assertEquals("Bank A", debts.get(0).getCreditor());
        verify(debtRepository, times(1)).getDebtsByUserId(userId);
    }

    @Test
    void testGetDebtsByUserId_NoDebts() {
        when(debtRepository.getDebtsByUserId(userId)).thenReturn(Collections.emptyList());

        List<Debt> debts = debtBean.getDebtsByUserId(userId);

        assertTrue(debts.isEmpty());
        verify(debtRepository, times(1)).getDebtsByUserId(userId);
    }

    @Test
    void testUpdateDebt_Success() {
        when(debtRepository.updateDebt(debtId, testDebt)).thenReturn(true);

        boolean updated = debtBean.updateDebt(debtId, testDebt);

        assertTrue(updated);
        verify(debtRepository, times(1)).updateDebt(debtId, testDebt);
    }

    @Test
    void testUpdateDebt_NotFound() {
        when(debtRepository.updateDebt(debtId, testDebt)).thenReturn(false);

        boolean updated = debtBean.updateDebt(debtId, testDebt);

        assertFalse(updated);
        verify(debtRepository, times(1)).updateDebt(debtId, testDebt);
    }

    @Test
    void testDeleteDebt_Success() {
        when(debtRepository.deleteDebt(debtId)).thenReturn(true);

        boolean deleted = debtBean.deleteDebt(debtId);

        assertTrue(deleted);
        verify(debtRepository, times(1)).deleteDebt(debtId);
    }

    @Test
    void testDeleteDebt_NotFound() {
        when(debtRepository.deleteDebt(debtId)).thenReturn(false);

        boolean deleted = debtBean.deleteDebt(debtId);

        assertFalse(deleted);
        verify(debtRepository, times(1)).deleteDebt(debtId);
    }

    @Test
    void testMarkAsPaid_Success() {
        when(debtRepository.getDebtsByUserId(debtId)).thenReturn(Arrays.asList(testDebt));

        boolean marked = debtBean.markAsPaid(debtId);

        assertTrue(marked);
        verify(debtRepository, times(1)).markAsPaid(debtId);
    }

    @Test
    void testMarkAsPaid_NotFound() {
        when(debtRepository.getDebtsByUserId(debtId)).thenReturn(Collections.emptyList());

        boolean marked = debtBean.markAsPaid(debtId);

        assertFalse(marked);
        verify(debtRepository, never()).markAsPaid(any());
    }

    @Test
    void testGetDebtById_Success() {
        when(debtRepository.getDebtById(debtId)).thenReturn(testDebt);

        Debt debt = debtBean.getDebtById(debtId);

        assertNotNull(debt);
        assertEquals("Bank A", debt.getCreditor());
        verify(debtRepository, times(1)).getDebtById(debtId);
    }

    @Test
    void testGetDebtById_NotFound() {
        when(debtRepository.getDebtById(debtId)).thenReturn(null);

        Debt debt = debtBean.getDebtById(debtId);

        assertNull(debt);
        verify(debtRepository, times(1)).getDebtById(debtId);
    }
}