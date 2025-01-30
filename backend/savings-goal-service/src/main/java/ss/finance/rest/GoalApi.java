package ss.finance.rest;

import java.util.List;
import java.util.Map;
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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import ss.finance.entities.SavingsGoal;
import ss.finance.security.JwtUtil;
import ss.finance.services.GoalBean;

@Tag(name = "Savings Goals", description = "Endpoints related to managing savings goals")
@Path("/savings-goals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GoalApi {

    @Inject
    private GoalBean goalBean;

    @Inject
    private JwtUtil jwtUtil;

    private static final Logger logger = Logger.getLogger(GoalApi.class.getName());
    private static final String MONGO_URI = System.getenv("MONGO_URI");
    private static final String DATABASE_NAME = System.getenv("DATABASE_NAME");

    @GET
    @Path("/health")
    public Response healthCheck() {
        boolean isMongoUp = checkMongoDB();
        boolean isServiceUp = true; // Če endpoint deluje, je storitev UP.

        JsonObjectBuilder healthBuilder = Json.createObjectBuilder()
                .add("status", isMongoUp ? "UP" : "DOWN");

        JsonObjectBuilder detailsBuilder = Json.createObjectBuilder()
                .add("MongoDB", isMongoUp ? "UP" : "DOWN")
                .add("Savings Goal Service", isServiceUp ? "UP" : "DOWN");

        JsonObject healthJson = healthBuilder.add("details", detailsBuilder.build()).build();

        int statusCode = isMongoUp ? Response.Status.OK.getStatusCode() : Response.Status.SERVICE_UNAVAILABLE.getStatusCode();
        return Response.status(statusCode).entity(healthJson.toString()).build();
    }

    private boolean checkMongoDB() {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            database.listCollectionNames().first(); // Če ne vrže izjeme, je MongoDB UP
            return true;
        } catch (Exception e) {
            logger.severe("MongoDB Health Check Failed: " + e.getMessage());
            return false;
        }
    }

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
    public Response addSavingsGoal(@CookieParam("auth_token") String token, @RequestBody(
            description = "Savings goal details including goalName, targetAmount, currentAmount, startDate, and deadline",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(
                            example = "{ \"goalName\": \"Vacation Fund\", \"targetAmount\": 5000.00, \"currentAmount\": 1000.00, \"startDate\": \"2025-01-01\", \"deadline\": \"2025-12-31\" }"
                    )
            )
    ) SavingsGoal goal) {
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
    public Response updateSavingsGoal(@CookieParam("auth_token") String token, @PathParam("id") String id, @RequestBody(
            description = "Updated savings goal",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(
                            example = "{  \"currentAmount\": 1500.00 \"}"
                    )
            )
    )SavingsGoal updatedGoal) {
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
