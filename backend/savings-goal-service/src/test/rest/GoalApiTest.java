package ss.finance.rest;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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

import ss.finance.entities.SavingsGoal;
import ss.finance.security.JwtUtil;
import ss.finance.services.GoalBean;

@ExtendWith(MockitoExtension.class)
class GoalApiTest {

    @Mock
    private GoalBean goalBean;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private GoalApi goalApi;

    private String validToken;
    private ObjectId userId;
    private SavingsGoal sampleGoal;

    @BeforeEach
    void setUp() {
        validToken = "valid.jwt.token";
        userId = new ObjectId();
    
        sampleGoal = new SavingsGoal();
        sampleGoal.setUserId(userId);
        sampleGoal.setGoalName("Vacation");
        sampleGoal.setTargetAmount(3000);
        sampleGoal.setCurrentAmount(500);
        sampleGoal.setStartDate(null);
        sampleGoal.setDeadline(null);
    }

    @Test
    void testGetSavingsGoals_Success() {
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(goalBean.getAllSavingsGoals(userId)).thenReturn(List.of(sampleGoal));

        Response response = goalApi.getSavingsGoals(validToken);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(goalBean, times(1)).getAllSavingsGoals(userId);
    }

    @Test
    void testGetSavingsGoals_Unauthorized() {
        Response response = goalApi.getSavingsGoals(null);

        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "Invalid token or user not found"), response.getEntity());
    }

    @Test
    void testAddSavingsGoal_Success() {
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);

        Response response = goalApi.addSavingsGoal(validToken, sampleGoal);

        assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(Map.of("message", "Savings goal created successfully"), response.getEntity());
        verify(goalBean, times(1)).addSavingsGoal(any(SavingsGoal.class));
    }

    @Test
    void testAddSavingsGoal_InvalidData() {
        SavingsGoal invalidGoal = new SavingsGoal(null, "", -100, -50, null, null);

        Response response = goalApi.addSavingsGoal(validToken, invalidGoal);

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "Missing or invalid fields. 'goalName', 'targetAmount', 'currentAmount', 'startDate', and 'deadline' are required."), response.getEntity());

        verify(goalBean, never()).addSavingsGoal(any(SavingsGoal.class));
    }

    @Test
    void testDeleteSavingsGoal_Success() {
        String goalId = new ObjectId().toHexString();
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(goalBean.deleteSavingsGoal(new ObjectId(goalId))).thenReturn(true);

        Response response = goalApi.deleteSavingsGoal(validToken, goalId);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals(Map.of("message", "Savings goal deleted successfully"), response.getEntity());

        verify(goalBean, times(1)).deleteSavingsGoal(new ObjectId(goalId));
    }

    @Test
    void testDeleteSavingsGoal_NotFound() {
        String goalId = new ObjectId().toHexString();
        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        when(goalBean.deleteSavingsGoal(new ObjectId(goalId))).thenReturn(false);

        Response response = goalApi.deleteSavingsGoal(validToken, goalId);

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "Savings goal not found"), response.getEntity());
    }

    @Test
    void testUpdateSavingsGoal_Success() {
        String goalId = new ObjectId().toHexString();
        SavingsGoal updatedGoal = new SavingsGoal(userId, "Updated Goal", 4000, 1000, null, null);

        when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
        doNothing().when(goalBean).updateSavingsGoal(any(ObjectId.class), any(SavingsGoal.class));

        Response response = goalApi.updateSavingsGoal(validToken, goalId, updatedGoal);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals(Map.of("message", "Savings goal updated successfully"), response.getEntity());

        verify(goalBean, times(1)).updateSavingsGoal(new ObjectId(goalId), updatedGoal);
    }

    @Test
    void testUpdateSavingsGoal_Unauthorized() {
        String goalId = new ObjectId().toHexString();
        SavingsGoal updatedGoal = new SavingsGoal(userId, "Updated Goal", 4000, 1000, null, null);

        Response response = goalApi.updateSavingsGoal(null, goalId, updatedGoal);

        assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals(Map.of("error", "Invalid token or user not found"), response.getEntity());
    }
}