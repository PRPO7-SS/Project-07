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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ss.finance.entities.Budget;
import ss.finance.security.JwtUtil;
import ss.finance.services.BudgetBean;
import ss.finance.services.BudgetUpdateRequest;

@Tag(name = "Budgets", description = "Endpoints for managing budgets")
@Path("/budget")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BudgetApi {

    @Inject
    private BudgetBean budgetBean;

    @Inject
    private JwtUtil jwtUtil;

    private static final Logger logger = Logger.getLogger(BudgetApi.class.getName());

    @Operation(summary = "Add a new budget",
            description = "Creates a new budget for the authenticated user.")
    @APIResponse(
            responseCode = "201",
            description = "Budget added successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Budget added successfully\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Token is missing or invalid\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Server error occurred\"}"))
    )
    @POST
    public Response addBudget(
            @RequestBody(description = "Budget data to be created", required = true, content = @Content(schema = @Schema(implementation = Budget.class))) Budget budget,
            @CookieParam("auth_token") String token) {
        try {
            logger.info("Received token: " + token);
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            budget.setUserId(userId);
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

    @Operation(summary = "Get all budgets",
            description = "Retrieves all budgets for the authenticated user.")
    @APIResponse(
            responseCode = "200",
            description = "List of budgets",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "[{\"category\": \"Groceries\", \"monthlyLimit\": 300, \"remainingBudget\": 150}]"))
    )
    @APIResponse(
            responseCode = "204",
            description = "No budgets found"
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Token is missing or invalid\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Server error occurred\"}"))
    )
    @GET
    public Response getBudgets(@CookieParam("auth_token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            var budgets = budgetBean.getBudgetsByUserId(userId);

            if (budgets.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            return Response.ok(budgets).build();
        } catch (Exception e) {
            logger.severe("Error retrieving budgets: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @Operation(summary = "Update a budget",
            description = "Updates the monthly limit of a specific budget by category.")
    @APIResponse(
            responseCode = "200",
            description = "Budget updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Budget updated successfully\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Token is missing or invalid\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Server error occurred\"}"))
    )
    @PUT
    @Path("/{categoryName}")
    public Response updateBudget(
            @PathParam("categoryName") String categoryName,
            @CookieParam("auth_token") String token,
            @RequestBody(description = "Request body containing the new monthly limit", required = true, content = @Content(schema = @Schema(implementation = BudgetUpdateRequest.class))) BudgetUpdateRequest request) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            String normalizedCategoryName = categoryName.substring(0, 1).toUpperCase() + categoryName.substring(1).toLowerCase();
            budgetBean.updateBudget(userId, normalizedCategoryName, request.getNewLimit());
            return Response.ok("{\"message\": \"Budget updated successfully\"}").build();
        } catch (Exception e) {
            logger.severe("Error updating budget: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @Operation(summary = "Delete a budget",
            description = "Deletes a budget by category.")
    @APIResponse(
            responseCode = "200",
            description = "Budget deleted successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Budget deleted successfully\"}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "Budget not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Budget not found for category: Groceries\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Server error occurred\"}"))
    )
    @DELETE
    @Path("/{categoryName}")
    public Response deleteBudget(@PathParam("categoryName") String categoryName, @CookieParam("auth_token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
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