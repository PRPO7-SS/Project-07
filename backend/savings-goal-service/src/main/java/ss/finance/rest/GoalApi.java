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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Savings Goals", description = "Endpoints related to managing savings goals")
@Path("/savings-goals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GoalApi {

    @Inject
    private GoalBean goalBean;

    @Inject
    private JwtUtil jwtUtil;

    @Operation(summary = "Get all savings goals", description = "Returns all savings goals for the authenticated user.")
    @APIResponse(
            responseCode = "200",
            description = "Savings goals retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SavingsGoal.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Invalid token or user not found\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Server error\"}"))
    )
    @GET
    public Response getSavingsGoals(@CookieParam("auth_token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Invalid token or user not found"))
                        .build();
            }
            ObjectId userId = jwtUtil.extractUserId(token);
            List<SavingsGoal> goals = goalBean.getAllSavingsGoals(userId);
            return Response.ok(goals).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Invalid token or user not found")).build();
        }
    }

    @Operation(summary = "Add a new savings goal", description = "Creates a new savings goal for the authenticated user.")
    @APIResponse(
            responseCode = "201",
            description = "Savings goal created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Savings goal created successfully\"}"))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid or missing data",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Missing or invalid fields. 'goalName', 'targetAmount', 'currentAmount', 'startDate', and 'deadline' are required.\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Invalid token or user not found\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Server error\"}"))
    )
    @POST
    public Response addSavingsGoal(@CookieParam("auth_token") String token, SavingsGoal goal) {
        try {
            if (goal.getGoalName() == null || goal.getGoalName().isEmpty() ||
                    goal.getTargetAmount() == null || goal.getTargetAmount() <= 0 ||
                    goal.getCurrentAmount() == null || goal.getCurrentAmount() < 0 ||
                    goal.getStartDate() == null || goal.getDeadline() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Missing or invalid fields. 'goalName', 'targetAmount', 'currentAmount', 'startDate', and 'deadline' are required."))
                        .build();
            }
            ObjectId userId = jwtUtil.extractUserId(token);
            goal.setUserId(userId);
            goalBean.addSavingsGoal(goal);
            return Response.status(Response.Status.CREATED).entity(Map.of("message", "Savings goal created successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Server error: " + e.getMessage()))
                    .build();
        }
    }

    @Operation(summary = "Update a savings goal", description = "Updates an existing savings goal for the authenticated user.")
    @APIResponse(
            responseCode = "200",
            description = "Savings goal updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Savings goal updated successfully\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Invalid token or user not found\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Server error\"}"))
    )
    @PUT
    @Path("/{id}")
    public Response updateSavingsGoal(@CookieParam("auth_token") String token, @PathParam("id") String id, SavingsGoal updatedGoal) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Invalid token or user not found"))
                        .build();
            }
            ObjectId userId = jwtUtil.extractUserId(token);
            updatedGoal.setUserId(userId);
            ObjectId goalId = new ObjectId(id);
            goalBean.updateSavingsGoal(goalId, updatedGoal);
            return Response.ok(Map.of("message", "Savings goal updated successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Server error"))
                    .build();
        }
    }

    @Operation(summary = "Delete a savings goal", description = "Deletes a specific savings goal by ID.")
    @APIResponse(
            responseCode = "204",
            description = "Savings goal deleted successfully"
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Invalid token or user not found\"}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "Savings goal not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Savings goal not found\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Server error\"}"))
    )
    @DELETE
    @Path("/{id}")
    public Response deleteSavingsGoal(@CookieParam("auth_token") String token, @PathParam("id") String id) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Invalid token or user not found"))
                        .build();
            }
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
