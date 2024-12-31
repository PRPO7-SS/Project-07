package ss.finance.rest;

import ss.finance.services.GoalBean;
import ss.finance.entities.SavingsGoal;
import ss.finance.security.JwtUtil;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;

@Path("/savings-goals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GoalApi {

    @Inject
    private GoalBean goalBean;

    @Inject
    private JwtUtil jwtUtil;

    @GET
    public Response getSavingsGoals(@CookieParam("auth-token") String token) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            List<SavingsGoal> goals = goalBean.getAllSavingsGoals(userId);
            return Response.ok(goals).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Invalid token or user not found")).build();
        }
    }

    @POST
    public Response addSavingsGoal(@CookieParam("auth-token") String token, SavingsGoal goal) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            goal.setUserId(userId);
            goalBean.addSavingsGoal(goal);
            return Response.status(Response.Status.CREATED).entity(Map.of("message", "Savings goal created successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateSavingsGoal(@CookieParam("auth-token") String token, @PathParam("id") String id, SavingsGoal updatedGoal) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            updatedGoal.setUserId(userId);
            ObjectId goalId = new ObjectId(id);
            goalBean.updateSavingsGoal(goalId, updatedGoal);
            return Response.ok(Map.of("message", "Savings goal updated successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteSavingsGoal(@CookieParam("auth-token") String token, @PathParam("id") String id) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            ObjectId goalId = new ObjectId(id);
            boolean deleted = goalBean.deleteSavingsGoal(goalId);

            if (deleted) {
                return Response.ok(Map.of("message", "Savings goal deleted successfully")).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Savings goal not found")).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }
}
