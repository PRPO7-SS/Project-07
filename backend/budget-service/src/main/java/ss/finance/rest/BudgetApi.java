package ss.finance.rest;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;

import ss.finance.entities.Budget;
import ss.finance.security.JwtUtil;
import ss.finance.services.BudgetBean;
import ss.finance.services.BudgetUpdateRequest;

@Path("/budget")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BudgetApi {

    @Inject
    private BudgetBean budgetBean;

    @Inject
    private JwtUtil jwtUtil;

    @Inject
    private BudgetUpdateRequest request;

    private static final Logger logger = Logger.getLogger(BudgetApi.class.getName());


    @POST
    public Response addBudget(Budget budget, @CookieParam("auth_token") String token) {
        try {
            logger.info("Received token: " + token);

            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            budget.setUserId(userId);

            // Normalizacija kategorije
            budget.setCategory(
                budget.getCategory().substring(0, 1).toUpperCase() + budget.getCategory().substring(1).toLowerCase()
            );

            budgetBean.addBudget(budget);
            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\": \"Budget added successfully\"}")
                    .build();
        } catch (Exception e) {
            logger.severe("Error adding budget: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @GET
    public Response getBudgets(@CookieParam("auth_token") String token) {
        try {
            logger.info("Received token: " + token);

            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            logger.info("Extracted userId: " + userId);

            var budgets = budgetBean.getBudgetsByUserId(userId);

            if (budgets.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            return Response.ok(budgets).build();
        } catch (Exception e) {
            logger.severe("Error retrieving transactions: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{categoryName}")
    public Response updateBudget(@PathParam("categoryName") String categoryName, @CookieParam("auth_token") String token, BudgetUpdateRequest request) {
        try {
            logger.info("Received token: " + token);
    
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }
    
            ObjectId userId = jwtUtil.extractUserId(token);
            logger.info("Extracted userId: " + userId);
    
            String normalizedCategoryName = categoryName.substring(0, 1).toUpperCase() + categoryName.substring(1).toLowerCase();
            double newLimit = request.getNewLimit();
            budgetBean.updateBudget(userId, normalizedCategoryName, newLimit);
            logger.info("Budget updated successfully for category: " + normalizedCategoryName);
    
            return Response.ok("{\"message\": \"Budget updated successfully\"}").build();
        } catch (Exception e) {
            logger.severe("Error updating budget: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{categoryName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBudget(@PathParam("categoryName") String categoryName, @CookieParam("auth_token") String token) {
        try {
            logger.info("Received request to delete budget for category: " + categoryName);
    
            // Validacija tokena
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }
    
            // Ekstrakcija userId iz tokena
            ObjectId userId = jwtUtil.extractUserId(token);
    
            // Izbriši budget iz baze
            boolean deleted = budgetBean.deleteBudget(userId, categoryName);
    
            if (deleted) {
                return Response.ok("{\"message\": \"Budget deleted successfully\"}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Budget not found for category: " + categoryName + "\"}")
                        .build();
            }
        } catch (Exception e) {
            logger.severe("Error deleting budget: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }
}