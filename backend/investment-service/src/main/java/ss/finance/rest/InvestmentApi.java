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


@Tag(name = "Investments", description = "Endpoints for managing investments")
@Path("/investments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InvestmentApi {

    @Inject
    private InvestmentBean investmentBean;

    @Inject
    private JwtUtil jwtUtil;

    @Operation(summary = "Get all investments",
            description = "Returns a list of all investments for the authenticated user")
    @APIResponse(responseCode = "200",
            description = "List of investments",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Investment.class)))
    @APIResponse(responseCode = "401",
            description = "Unauthorized access")
    @GET
    public Response getInvestments(@CookieParam("auth_token") String token) {
        System.out.println("cookie: " + token);
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Cookie is missing or invalid\"}")
                        .build();
            }
            ObjectId userId = jwtUtil.extractUserId(token);
            List<Investment> investments = investmentBean.getAllInvestments(userId);
            return Response.ok(investments).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Invalid token or user not found")).build();
        }
    }

    @Operation(summary = "Get investment by ID",
            description = "Fetches details of a specific investment by ID")
    @APIResponse(responseCode = "200",
            description = "Investment details",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Investment.class)))
    @APIResponse(responseCode = "404",
            description = "Investment not found")
    @GET
    @Path("/{id}")
    public Response getInvestmentById(@CookieParam("auth_token") String token, @PathParam("id") String id) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            ObjectId investmentId = new ObjectId(id);
            Investment investment = investmentBean.getInvestment(investmentId);

            if (investment != null && investment.getUserId().equals(userId)) {
                return Response.ok(investment).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Investment not found or access denied")).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @Operation(summary = "Create a new investment",
            description = "Adds a new investment for the authenticated user")
    @APIResponse(responseCode = "201",
            description = "Investment created")
    @POST
    public Response addInvestment(@CookieParam("auth_token") String token, Investment investment) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            investment.setUserId(userId);
            investmentBean.addInvestment(investment);
            return Response.status(Response.Status.CREATED).entity(Map.of("message", "Investment created successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @Operation(summary = "Update investment",
            description = "Updates an existing investment")
    @APIResponse(responseCode = "200",
            description = "Investment updated")
    @PUT
    @Path("/{id}")
    public Response updateInvestment(@CookieParam("auth_token") String token, @PathParam("id") String id, Investment updatedInvestment) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            updatedInvestment.setUserId(userId);
            ObjectId investmentId = new ObjectId(id);
            investmentBean.updateInvestment(investmentId, updatedInvestment);
            return Response.ok(Map.of("message", "Investment updated successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @Operation(summary = "Delete investment",
            description = "Deletes an investment by ID")
    @APIResponse(responseCode = "200",
            description = "Investment deleted")
    @DELETE
    @Path("/{id}")
    public Response deleteInvestment(@CookieParam("auth_token") String token, @PathParam("id") String id) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            ObjectId investmentId = new ObjectId(id);
            boolean deleted = investmentBean.deleteInvestment(investmentId);

            if (deleted) {
                return Response.ok(Map.of("message", "Investment deleted successfully")).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Investment not found")).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }
}
