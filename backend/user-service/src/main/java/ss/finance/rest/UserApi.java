package ss.finance.rest;

import ss.finance.services.UserBean;
import ss.finance.entities.User;
import ss.finance.security.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import io.jsonwebtoken.Claims;
import javax.ws.rs.core.NewCookie;
import org.bson.types.ObjectId;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserApi {

    @Inject
    private UserBean userBean;

    @Inject
    private JwtUtil jwtUtil;

    // Get all Users
    @GET
    public Response getAllUsers() {
        // Retrieve all users directly as User entities
        List<User> users = userBean.getAllUsers(); // Assuming the method now returns List<User>
        return Response.ok(users).build();
    }


    @PUT
    @Path("/change-password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(@CookieParam("auth_token") String token, Map<String, String> payload) {
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");
        try {

            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Cookie is missing or invalid\"}")
                        .build();
            }
            ObjectId userId = jwtUtil.extractUserId(token);
            User user = userBean.getUserById(userId);
            // Step 1: Validate input parameters
            if (oldPassword == null || newPassword == null || oldPassword.isEmpty() || newPassword.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Parameters 'oldPassword' and 'newPassword' are required\"}")
                        .build();
            }

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"User not found\"}")
                        .build();
            }

            // Step 4: Validate the old password
            if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Password does not match\"}")
                        .build();
            }

            // Step 5: Hash the new password and update the user record
            String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPassword(hashedNewPassword);
            ObjectId id = userBean.getUserId(user);
            userBean.updateUser(id, user);

            return Response.status(Response.Status.OK)
                    .entity("{\"message\": \"Password changed successfully\"}")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error occurred while changing password\"}")
                    .build();
        }
    }


    // Delete User by ID
    @DELETE
    @Path("/profile")
    public Response deleteUser(@HeaderParam("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Authorization header missing or invalid\"}")
                        .build();
            }

            // Extract token from Authorization header
            String token = authHeader.substring(7);  // Remove "Bearer " prefix

            // Extract the userId from the JWT token
            ObjectId userId = jwtUtil.extractUserId(token);  // Use the helper method to extract userId from the token

            // Delete the user by the extracted userId
            boolean isDeleted = userBean.deleteUser(userId);

            if (isDeleted) {
                return Response.noContent().build();  // Successfully deleted the user
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"User not found\"}")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error\"}")
                    .build();
        }
    }



    @GET
    @Path("/profile")
    public Response getUserProfile(@CookieParam("auth_token") String token) {
        try {
            System.out.println("Received token: " + token);
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            // Extract claims from the JWT token
            //Claims claims = jwtUtil.extractClaims(token);
            ObjectId userId = jwtUtil.extractUserId(token); // Extract userId from JWT token

            // Fetch the user using the userId
            User user = userBean.getUserById(userId);
            if (user != null) {
                return Response.ok(user).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"User not found\"}")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid token\"}")
                    .build();
        }
    }


    @PUT
    @Path("/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@CookieParam("auth_token")  String token, User updatedUser) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Cookie is missing or invalid\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);

            // Fetch the existing user
            User existingUser = userBean.getUserById(userId);
            if (existingUser == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"User not found\"}")
                        .build();
            }

            userBean.updateUser(userId, updatedUser);

            return Response.status(Response.Status.OK)
                    .entity("{\"message\": \"User updated successfully\"}")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error occurred while updating user\"}")
                    .build();
        }
    }

}
