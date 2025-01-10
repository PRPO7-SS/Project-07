package ss.finance.rest;

import java.util.Date;
import java.util.List;
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

import ss.finance.entities.Debt;
import ss.finance.security.JwtUtil;
import ss.finance.services.DebtBean;

@Tag(name = "Debts", description = "Endpoints for managing debts")
@Path("/debts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DebtApi {

    @Inject
    private DebtBean debtBean;

    @Inject
    private JwtUtil jwtUtil;

    private static final Logger logger = Logger.getLogger(DebtApi.class.getName());

    @Operation(summary = "Add a debt", description = "Creates a new debt for the authenticated user")
    @APIResponse(
            responseCode = "201",
            description = "Debt created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Debt added successfully\"}"))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid input",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Deadline cannot be in the past\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Token is missing or invalid\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Server error occurred\"}"))
    )
    @POST
    public Response addDebt(
            @RequestBody(description = "Debt details", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Debt.class))) 
            Debt debt, 
            @CookieParam("auth_token") String token) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            debt.setUserId(userId);

            if (debt.getDeadline() != null && debt.getDeadline().before(new Date())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Deadline cannot be in the past\"}")
                        .build();
            }

            debtBean.addDebt(debt);
            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\": \"Debt added successfully\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @Operation(summary = "Get all debts", description = "Retrieves all debts for the authenticated user")
    @APIResponse(
            responseCode = "200",
            description = "List of debts",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "[{\"id\": \"<debt_id>\", \"creditor\": \"Bank A\", \"amount\": 2000, \"deadline\": \"2023-12-01\"}]"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Token is missing or invalid\"}"))
    )
    @GET
    public Response getDebts(@CookieParam("auth_token") String token) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            List<Debt> debts = debtBean.getDebtsByUserId(userId);

            if (debts.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            return Response.ok(debts).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @Operation(summary = "Get a debt by ID", description = "Retrieves a specific debt by its ID")
    @APIResponse(
            responseCode = "200",
            description = "Debt details",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"id\": \"<debt_id>\", \"creditor\": \"Bank A\", \"amount\": 2000, \"deadline\": \"2023-12-01\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Token is missing or invalid\"}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "Debt not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Debt not found or does not belong to the user\"}"))
    )
    @GET
    @Path("/{id}")
    public Response getDebtById(@PathParam("id") String id, @CookieParam("auth_token") String token) {
        try {
            if (id == null || id.length() != 24 || !id.matches("[a-fA-F0-9]{24}")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Invalid debt ID format\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            ObjectId debtObjectId = new ObjectId(id);
            Debt debt = debtBean.getDebtById(debtObjectId);

            if (debt == null || !debt.getUserId().equals(userId)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Debt not found or does not belong to the user\"}")
                        .build();
            }

            return Response.ok(debt).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @Operation(summary = "Update a debt", description = "Updates an existing debt by its ID")
    @APIResponse(
            responseCode = "200",
            description = "Debt updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Debt updated successfully\"}"))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid debt ID",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Invalid debt ID\"}"))
    )
    @PUT
    @Path("/{debtId}")
    public Response updateDebt(@PathParam("debtId") String debtId, Debt updatedDebt, @CookieParam("auth_token") String token) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            ObjectId debtObjectId = new ObjectId(debtId);
            Debt existingDebt = debtBean.getDebtById(debtObjectId);

            if (existingDebt == null || !existingDebt.getUserId().equals(userId)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Debt not found or does not belong to the user\"}")
                        .build();
            }

            boolean updated = debtBean.updateDebt(debtObjectId, updatedDebt);

            if (updated) {
                return Response.ok("{\"message\": \"Debt updated successfully\"}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Debt not found\"}")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @Operation(summary = "Delete a debt", description = "Deletes a specific debt by its ID")
    @APIResponse(
            responseCode = "200",
            description = "Debt deleted successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Debt deleted successfully\"}"))
    )
    @DELETE
    @Path("/{debtId}")
    public Response deleteDebt(@PathParam("debtId") String debtId) {
        boolean deleted = debtBean.deleteDebt(new ObjectId(debtId));
        if (deleted) {
            return Response.ok("{\"message\": \"Debt deleted successfully\"}").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Debt not found\"}").build();
        }
    }

    @Operation(summary = "Mark a debt as paid", description = "Marks a specific debt as paid by its ID")
    @APIResponse(
            responseCode = "200",
            description = "Debt marked as paid successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Debt marked as paid successfully\"}"))
    )
    @PUT
    @Path("/{debtId}/markAsPaid")
    public Response markDebtAsPaid(@PathParam("debtId") String debtId) {
        try {
            debtBean.markAsPaid(new ObjectId(debtId));
            return Response.ok("{\"message\": \"Debt marked as paid successfully\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }
}