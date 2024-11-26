package ss.finance.rest;

import ss.finance.services.UserBean;
import ss.finance.entities.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserApi {

    @Inject
    private UserBean userBean;

    // Register User
    @POST
    @Path("/register")
    public Response addUser(User user) {
        try {
            // Hash the password before saving
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);

            // Add user to the database
            userBean.addUser(user);

            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\": \"User created successfully\"}")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Error creating user\"}")
                    .build();
        }
    }

    // Get all Users
    @GET
    public Response getAllUsers() {
        // Retrieve all users directly as User entities
        List<User> users = userBean.getAllUsers(); // Assuming the method now returns List<User>
        return Response.ok(users).build();
    }

    // Delete User by ID
    @DELETE
    @Path("/{userId}")
    public Response deleteUser(@PathParam("userId") String userId) {
        // Delete user by ID
        userBean.deleteUser(userId);
        return Response.noContent().build();
    }

    // Validate User Login
    @POST
    @Path("/login")
    public Response loginUser(User user) {
        try {
            // Validate user credentials (email and password)
            User validUser = userBean.validateUser(user.getEmail(), user.getPassword());

            if (validUser != null) {
                // You can return a response with user data or generate a JWT token here.
                return Response.ok(validUser).build(); // Or return a JWT token for authentication
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Invalid credentials\"}")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error\"}")
                    .build();
        }
    }
}
