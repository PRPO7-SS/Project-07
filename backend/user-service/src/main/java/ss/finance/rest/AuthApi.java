package ss.finance.rest;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;

import io.jsonwebtoken.Claims;
import ss.finance.entities.User;
import ss.finance.security.JwtUtil;
import ss.finance.services.UserBean;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthApi {
    @Inject
    private UserBean userBean;

    @Inject
    private JwtUtil jwtUtil;

    // Register User
    @POST
    @Path("/register")
    public Response addUser(User user) {
        try {
            // Validate mandatory fields
            if (user.getUsername() == null || user.getUsername().isEmpty() ||
                    user.getFullName() == null || user.getFullName().isEmpty() ||
                    user.getEmail() == null || user.getEmail().isEmpty() ||
                    user.getPassword() == null || user.getPassword().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Missing required fields\"}")
                        .build();
            }

            // Check if user with this email already exists
            if (userBean.existingUser(user.getEmail())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("{\"message\": \"A user with this email already exists\"}")
                        .build();
            }

            // Hash the password
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);

            // Add the user
            userBean.addUser(user);

            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\": \"User created successfully\"}")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error creating user\"}")
                    .build();
        }
    }


    // Validate User Login
    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON) // Ensure JSON response type
    public Response loginUser(User user) {
        try {
            User validUser = userBean.validateUser(user.getEmail(), user.getPassword());

            if (validUser != null) {
                ObjectId userId = userBean.getUserId(validUser.getEmail());
                String accessToken = jwtUtil.generateToken(userId, validUser.getEmail());
                String refreshToken = jwtUtil.generateRefreshToken(userId, validUser.getEmail());

                // Create cookies for tokens
                NewCookie accessTokenCookie = new NewCookie("auth_token", accessToken, "/", "localhost", "Access token", 3600, false);
                NewCookie refreshTokenCookie = new NewCookie("refresh_token", refreshToken, "/", "localhost", "Refresh token", 24 * 3600, true);

                // Properly format JSON response
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("message", "Login successful");
                responseBody.put("accessToken", accessToken);
                responseBody.put("refreshToken", refreshToken);

                return Response.status(Response.Status.OK)
                        .cookie(accessTokenCookie, refreshTokenCookie)
                        .entity(responseBody) // JAX-RS will serialize the Map to JSON
                        .build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Invalid credentials\"}")
                        .header("Content-Type", "application/json")
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error\"}")
                    .header("Content-Type", "application/json")
                    .build();
        }
    }




    // Logout User
    @POST
    @Path("/logout")
    public Response logOut() {
        NewCookie expiredAccessToken = new NewCookie("auth_token", "", "/", null, "auth_token", 0, false);
        NewCookie expiredRefreshToken = new NewCookie("refresh_token", "", "/", null, "refresh_token", 0, false);

        return Response.status(Response.Status.OK)
                .cookie(expiredAccessToken, expiredRefreshToken)
                .entity("{\"message\": \"Logout successful\"}")
                .build();
    }


    @POST
    @Path("/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshToken(@CookieParam("refresh_token") String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Refresh token not found.\"}")
                    .build();
        }

        try {
            ObjectId userId = jwtUtil.extractRefreshUserId(refreshToken);

            User user = userBean.getUserById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"User not found.\"}")
                        .build();
            }

            String newAccessToken = jwtUtil.generateToken(userId, user.getEmail());

            NewCookie accessTokenCookie = new NewCookie("auth_token", newAccessToken, "/", null, "Access token", 3600000, false);

            return Response.ok("{\"message\": \"Token refreshed successfully\"}")
                    .cookie(accessTokenCookie)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Invalid refresh token.\"}")
                    .build();
        }
    }

    @GET
    @Path("/check-refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkRefreshToken(@CookieParam("refresh_token") String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"valid\": false}")
                    .build();
        }

        try {
            ObjectId userId = jwtUtil.extractRefreshUserId(refreshToken);

            User user = userBean.getUserById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"valid\": false}")
                        .build();
            }
            return  Response.ok("{\"valid\": true}").build();
        }catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Refresh token expired\"}")
                    .build();
        }

    }







}
