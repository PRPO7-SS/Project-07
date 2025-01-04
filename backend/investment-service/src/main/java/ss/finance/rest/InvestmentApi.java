package ss.finance.rest;

import ss.finance.services.InvestmentBean;
import ss.finance.entities.Investment;
import ss.finance.security.JwtUtil;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.logging.Logger;


@Tag(name = "Investments", description = "Endpoints for managing investments")
@Path("/investments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InvestmentApi {

    @Inject
    private InvestmentBean investmentBean;

    @Inject
    private JwtUtil jwtUtil;

    private static final Logger logger = Logger.getLogger(InvestmentApi.class.getName());

    @Operation(summary = "Get all investments",
            description = "Returns a list of all investments for the authenticated user")
    @APIResponse(
            responseCode = "200",
            description = "List of investments",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "[{\"id\": \"<investment_id>\", \"type\": \"crypto\", \"name\": \"Bitcoin\", \"amount\": 500.0, \"quantity\": 0.01, \"purchaseDate\": \"2023-12-01\"}]"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Unauthorized access. Missing or invalid cookie.\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Internal server error. Please try again later.\"}"))
    )
    @GET
    public Response getInvestments(@CookieParam("auth_token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Unauthorized access. Missing or invalid cookie."))
                        .build();
            }
            ObjectId userId = jwtUtil.extractUserId(token);
            List<Investment> investments = investmentBean.getAllInvestments(userId);
            return Response.ok(investments).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid token."))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Internal server error. Please try again later."))
                    .build();
        }
    }

    @Operation(summary = "Get investment by ID",
            description = "Fetches details of a specific investment by ID")
    @APIResponse(
            responseCode = "200",
            description = "Investment details",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"id\": \"<investment_id>\", \"type\": \"crypto\", \"name\": \"Bitcoin\", \"amount\": 500.0, \"quantity\": 0.01, \"purchaseDate\": \"2023-12-01\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Unauthorized access. Missing or invalid cookie.\"}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "Investment not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Investment not found or access denied.\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Internal server error. Please try again later.\"}"))
    )
    @GET
    @Path("/{id}")
    public Response getInvestmentById(@CookieParam("auth_token") String token, @PathParam("id") String id) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Unauthorized access. Missing or invalid cookie."))
                        .build();
            }
            ObjectId userId = jwtUtil.extractUserId(token);
            ObjectId investmentId = new ObjectId(id);
            Investment investment = investmentBean.getInvestment(investmentId);

            if (investment != null && investment.getUserId().equals(userId)) {
                return Response.ok(investment).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Investment not found or access denied."))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid investment ID format."))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Internal server error. Please try again later."))
                    .build();
        }
    }


    @Operation(summary = "Create a new investment",
            description = "Adds a new investment for the authenticated user")
    @APIResponse(
            responseCode = "201",
            description = "Investment created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Investment created successfully\"}"))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid or missing body parameters",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Body parameters 'type', 'name', 'amount', 'quantity', and 'purchaseDate' are required.\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Internal server error. Please try again later.\"}"))
    )
    @POST
    public Response addInvestment(@CookieParam("auth_token") String token, Investment investment) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            investment.setUserId(userId);

            if (investment.getType() == null || investment.getType().isEmpty() ||
                investment.getName() == null || investment.getName().isEmpty() ||
                investment.getAmount() == null || investment.getAmount() <= 0 ||
                investment.getQuantity() == null || investment.getQuantity() <= 0 ||
                investment.getPurchaseDate() == null) {

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Body parameters 'type', 'name', 'amount', 'quantity', and 'purchaseDate' are required."))
                        .build();
            }

            investmentBean.addInvestment(investment);
            return Response.status(Response.Status.CREATED).entity(Map.of("message", "Investment created successfully")).build();
        } catch (IllegalArgumentException e) {
            logger.warning("Validation failed: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            logger.severe("Unexpected error occurred: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Internal server error."))
                    .build();
        }
    }


    @Operation(summary = "Delete investment",
            description = "Deletes an investment by ID")
    @APIResponse(
            responseCode = "204",
            description = "Investment deleted successfully"
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid investment ID",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Invalid investment ID format.\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Unauthorized access. Missing or invalid cookie.\"}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "Investment not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Investment not found.\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Server error occurred",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"error\": \"Internal server error. Please try again later.\"}"))
    )
    @DELETE
    @Path("/{id}")
    public Response deleteInvestment(@CookieParam("auth_token") String token, @PathParam("id") String id) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Unauthorized access. Missing or invalid cookie."))
                        .build();
            }
            ObjectId userId = jwtUtil.extractUserId(token);
            ObjectId investmentId = new ObjectId(id);
            boolean deleted = investmentBean.deleteInvestment(investmentId);

            if (deleted) {
                return Response.noContent().build(); // 204 No Content
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Investment not found."))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid investment ID format."))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Internal server error. Please try again later."))
                    .build();
        }
    }

}
