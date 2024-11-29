package ss.finance.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ss.finance.entities.SavingsGoal;
import ss.finance.services.SavingsGoalBean;

@Path("/savings-goals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SavingsGoalApi {

    @Inject
    private SavingsGoalBean savingsGoalBean;

    // Create Savings Goal
    @POST
    public Response addSavingsGoal(SavingsGoal savingsGoal) {
        try {
            // Add savings goal to the database
            savingsGoalBean.addSavingsGoal(savingsGoal);

            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\": \"Savings goal created successfully\"}")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Error creating savings goal\"}")
                    .build();
        }
    }

    // Get All Savings Goals
    @GET
    public Response getAllSavingsGoals() {
        try {
            List<SavingsGoal> savingsGoals = savingsGoalBean.getAllSavingsGoals();
            return Response.ok(savingsGoals).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error retrieving savings goals\"}")
                    .build();
        }
    }

    // Get Savings Goal by ID
    @GET
    @Path("/{goalId}")
    public Response getSavingsGoalById(@PathParam("goalId") String goalId) {
        try {
            SavingsGoal savingsGoal = savingsGoalBean.getSavingsGoalById(goalId);

            if (savingsGoal == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Savings goal not found\"}")
                        .build();
            }

            return Response.ok(savingsGoal).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error retrieving savings goal\"}")
                    .build();
        }
    }

    // Update Savings Goal
    @PUT
    @Path("/{goalId}")
    public Response updateSavingsGoal(@PathParam("goalId") String goalId, SavingsGoal savingsGoal) {
        try {
            savingsGoal.setId(new org.bson.types.ObjectId(goalId));
            savingsGoalBean.updateSavingsGoal(savingsGoal);

            return Response.ok("{\"message\": \"Savings goal updated successfully\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Error updating savings goal\"}")
                    .build();
        }
    }

    // Delete Savings Goal by ID
    @DELETE
    @Path("/{goalId}")
    public Response deleteSavingsGoal(@PathParam("goalId") String goalId) {
        try {
            savingsGoalBean.deleteSavingsGoal(goalId);

            return Response.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error deleting savings goal\"}")
                    .build();
        }
    }
}
