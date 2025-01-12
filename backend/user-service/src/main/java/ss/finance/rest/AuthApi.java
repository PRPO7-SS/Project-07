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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;


@Tag(name = "Authentication", description = "Endpoints related to user authentication")
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthApi {

    @Inject
    private UserBean userBean;

    @Inject
    private JwtUtil jwtUtil;

    @Operation(summary = "Register a new user", description = "Creates a new user account.")
    @APIResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"User created successfully\"}"))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid or missing fields",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Missing required fields\"}"))
    )
    @APIResponse(
            responseCode = "409",
            description = "User already exists",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"A user with this email already exists\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Error creating user\"}"))
    )
    @POST
    @Path("/register")
    public Response addUser(@RequestBody(
            description = "User details including fullName, email, username, password, and dateOfBirth",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(
                            example = "{ \"fullName\": \"Samo Primer\", \"email\": \"samo.primer@example.com\", \"username\": \"samoprimer\", \"password\": \"securepassword123\", \"dateOfBirth\": \"1990-05-15\" }"
                    )
            )
    )User user) {
        try {
            if (user.getUsername() == null || user.getUsername().isEmpty() ||
                    user.getFullName() == null || user.getFullName().isEmpty() ||
                    user.getEmail() == null || user.getEmail().isEmpty() ||
                    user.getPassword() == null || user.getPassword().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Missing required fields"))
                        .build();
            }

            if (userBean.existingUser(user.getEmail())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("error", "A user with this email already exists"))
                        .build();
            }

            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);
            userBean.addUser(user);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message", "User created successfully"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error creating user"))
                    .build();
        }
    }

    @Operation(summary = "Login a user", description = "Validates user credentials and returns tokens.")
    @APIResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Login successful\", \"accessToken\": \"...\", \"refreshToken\": \"...\"}"))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid or missing credentials",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Email and password are required.\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Invalid credentials\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Server error\"}"))
    )
    @POST
    @Path("/login")
    public Response loginUser(User user) {
        try {
            if (user.getEmail() == null || user.getEmail().isEmpty() ||
                    user.getPassword() == null || user.getPassword().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Email and password are required."))
                        .build();
            }

            User validUser = userBean.validateUser(user.getEmail(), user.getPassword());
            if (validUser != null) {
                ObjectId userId = userBean.getUserId(validUser.getEmail());
                String accessToken = jwtUtil.generateToken(userId, validUser.getEmail());
                String refreshToken = jwtUtil.generateRefreshToken(userId, validUser.getEmail());

                NewCookie accessTokenCookie = new NewCookie("auth_token", accessToken, "/", null, "Access token", 3600, false, true);
                NewCookie refreshTokenCookie = new NewCookie("refresh_token", refreshToken, "/", null, "Refresh token", 24 * 3600, false, true);

                return Response.ok(Map.of(
                                "message", "Login successful",
                                "accessToken", accessToken,
                                "refreshToken", refreshToken))
                        .cookie(accessTokenCookie, refreshTokenCookie)
                        .build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Invalid credentials or user not found"))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Server error"))
                    .build();
        }
    }

    @Operation(summary = "Logout user", description = "Logs out the user by expiring the tokens.")
    @APIResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Logout successful\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Server error\"}"))
    )
    @POST
    @Path("/logout")
    public Response logOut() {
        try {
            NewCookie expiredAccessToken = new NewCookie("auth_token", "", "/", null, "auth_token", 0, false);
            NewCookie expiredRefreshToken = new NewCookie("refresh_token", "", "/", null, "refresh_token", 0, false);

            return Response.status(Response.Status.OK)
                    .cookie(expiredAccessToken, expiredRefreshToken)
                    .entity(Map.of("message", "Logout successful"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Server error"))
                    .build();
        }
    }

    @Operation(summary = "Refresh token", description = "Generates a new access token using the refresh token.")
    @APIResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Token refreshed successfully\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Invalid or missing refresh token",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Invalid refresh token.\"}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"User not found.\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Server error\"}"))
    )
    @POST
    @Path("/refresh")
    public Response refreshToken(@CookieParam("refresh_token") String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Refresh token not found."))
                    .build();
        }

        try {
            ObjectId userId = jwtUtil.extractRefreshUserId(refreshToken);

            User user = userBean.getUserById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "User not found."))
                        .build();
            }

            String newAccessToken = jwtUtil.generateToken(userId, user.getEmail());
            NewCookie accessTokenCookie = new NewCookie("auth_token", newAccessToken, "/", null, "Access token", 3600, false);

            return Response.ok(Map.of("message", "Token refreshed successfully"))
                    .cookie(accessTokenCookie)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Server error"))
                    .build();
        }
    }

    @Operation(summary = "Check refresh token validity", description = "Checks if the refresh token is valid.")
    @APIResponse(
            responseCode = "200",
            description = "Valid refresh token",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"valid\": true}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Invalid or missing refresh token",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"valid\": false}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"valid\": false}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Server error\"}"))
    )
    @GET
    @Path("/check-refresh")
    public Response checkRefreshToken(@CookieParam("refresh_token") String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("valid", false))
                    .build();
        }

        try {
            ObjectId userId = jwtUtil.extractRefreshUserId(refreshToken);
            User user = userBean.getUserById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("valid", false))
                        .build();
            }
            return Response.ok(Map.of("valid", true)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Server error"))
                    .build();
        }
    }
}
