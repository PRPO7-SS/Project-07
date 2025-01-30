package ss.finance.rest;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
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
import org.mindrot.jbcrypt.BCrypt;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import ss.finance.entities.User;
import ss.finance.security.JwtUtil;
import ss.finance.services.UserBean;

@Tag(name = "User", description = "Endpoints related to user management")
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserApi {

    @Inject
    private UserBean userBean;

    @Inject
    private JwtUtil jwtUtil;

    private static final Logger logger = Logger.getLogger(UserApi.class.getName());
    
    private static final String MONGO_URI = System.getenv("MONGO_URL");
    private static final String DATABASE_NAME = System.getenv("DATABASE_NAME");

    @GET
    @Path("/health")
    public Response healthCheck() {
        boolean isMongoUp = checkMongoDB();
        boolean isServiceUp = true; // Če endpoint deluje, pomeni, da je storitev UP

        JsonObjectBuilder healthBuilder = Json.createObjectBuilder()
                .add("status", isMongoUp ? "UP" : "DOWN");

        JsonObjectBuilder detailsBuilder = Json.createObjectBuilder()
                .add("MongoDB", isMongoUp ? "UP" : "DOWN")
                .add("User Service", isServiceUp ? "UP" : "DOWN");

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
            logger.severe("MongoDB connection failed: " + e.getMessage());
            return false;
        }
    }

    @Operation(summary = "Get all users", description = "Returns a list of all registered users.")
    @APIResponse(
            responseCode = "200",
            description = "List of users retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = User.class))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Server error\"}"))
    )
    @GET
    public Response getAllUsers() {
        try {
            List<User> users = userBean.getAllUsers();
            return Response.ok(users).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Server error"))
                    .build();
        }
    }

    @Operation(summary = "Change user password", description = "Allows the authenticated user to change their password.")
    @APIResponse(
            responseCode = "200",
            description = "Password changed successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Password changed successfully\"}"))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid or missing fields",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Parameters 'oldPassword', 'newPassword' and 'confirmNewPassword' are required\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Cookie is missing or invalid\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Error occurred while changing password\"}"))
    )
    @PUT
    @Path("/change-password")
    public Response changePassword(@CookieParam("auth_token") String token,@RequestBody(
            description = "Payload containing old password, new password, and confirm new password",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(
                            example = "{ \"oldPassword\": \"current-password\", \"newPassword\": \"new-password\", \"confirmNewPassword\": \"new-password\" }"
                    )
            )
    ) Map<String, String> payload) {
        try {
            String oldPassword = payload.get("oldPassword");
            String newPassword = payload.get("newPassword");
            String confirmNewPassword = payload.get("confirmNewPassword");

            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Cookie is missing or invalid"))
                        .build();
            }

            if (oldPassword == null || oldPassword.isEmpty() ||
                    newPassword == null || newPassword.isEmpty() ||
                    confirmNewPassword == null || confirmNewPassword.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Parameters 'oldPassword', 'newPassword' and 'confirmNewPaswword' are required"))
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            User user = userBean.getUserById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build();
            }

            if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Wrong current password"))
                        .build();
            }

            if(!newPassword.equals(confirmNewPassword)){
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Passwords do not match"))
                        .build();
            }

            String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPassword(hashedNewPassword);
            userBean.updateUser(userId, user);

            return Response.ok(Map.of("message", "Password changed successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error occurred while changing password"))
                    .build();
        }
    }

    @Operation(summary = "Delete user account", description = "Deletes the authenticated user's account.")
    @APIResponse(
            responseCode = "204",
            description = "User deleted successfully"
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Cookie is missing or invalid\"}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"User not found\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Server error\"}"))
    )
    @DELETE
    @Path("/profile")
    public Response deleteUser(@CookieParam("auth_token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Cookie is missing or invalid"))
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);

            boolean isDeleted = userBean.deleteUser(userId);
            if (isDeleted) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Server error"))
                    .build();
        }
    }

    @Operation(summary = "Get user profile", description = "Returns the authenticated user's profile information.")
    @APIResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = User.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Token is missing or invalid\"}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"User not found\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Server error\"}"))
    )
    @GET
    @Path("/profile")
    public Response getUserProfile(@CookieParam("auth_token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Token is missing or invalid"))
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            User user = userBean.getUserById(userId);
            if (user != null) {
                return Response.ok(user).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Server error"))
                    .build();
        }
    }

    @Operation(summary = "Update user profile", description = "Updates the authenticated user's profile information.")
    @APIResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"User updated successfully\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Cookie is missing or invalid\"}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"User not found\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Error occurred while updating user\"}"))
    )
    @PUT
    @Path("/profile")
    public Response updateUser(@CookieParam("auth_token") String token, User updatedUser) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Cookie is missing or invalid"))
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            User existingUser = userBean.getUserById(userId);
            if (existingUser == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found"))
                        .build();
            }

            userBean.updateUser(userId, updatedUser);
            return Response.ok(Map.of("message", "User updated successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error occurred while updating user"))
                    .build();
        }
    }
}
