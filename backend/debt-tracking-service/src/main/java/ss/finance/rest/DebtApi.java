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

import ss.finance.entities.Debt;
import ss.finance.security.JwtUtil;
import ss.finance.services.DebtBean;

@Path("/debts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DebtApi {

    @Inject
    private DebtBean debtBean;

    @Inject
    private JwtUtil jwtUtil;

    private static final Logger logger = Logger.getLogger(DebtApi.class.getName());

    @POST
    public Response addDebt(Debt debt, @CookieParam("auth_token") String token) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            debt.setUserId(userId);
    
            // Preverjanje za deadline
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

    @GET
    public Response getDebts(@CookieParam("auth_token") String token) {
        ObjectId userId = jwtUtil.extractUserId(token);
        List<Debt> debts = debtBean.getDebtsByUserId(userId);
        if (debts.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.ok(debts).build();
    }

    @GET
    @Path("/{id}")
    public Response getDebtById(@PathParam("id") String id, @CookieParam("auth_token") String token) {
        try {
            // Check if the token is valid
            logger.info("Received token: " + token);
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            // Validate the provided ID
            if (id == null || id.length() != 24 || !id.matches("[a-fA-F0-9]{24}")) {
                logger.warning("Invalid debt ID format: " + id);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Invalid debt ID format\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            logger.info("Extracted userId: " + userId);

            ObjectId debtObjectId = new ObjectId(id);
            Debt debt = debtBean.getDebtById(debtObjectId);

            // Check if the debt exists and belongs to the user
            if (debt == null || !debt.getUserId().equals(userId)) {
                logger.warning("Debt not found or does not belong to the user: " + id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Debt not found or does not belong to the user\"}")
                        .build();
            }

            logger.info("Fetched debt: " + debt);
            return Response.ok(debt).build();
        } catch (Exception e) {
            logger.severe("Error retrieving debt: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{debtId}")
    public Response updateDebt(@PathParam("debtId") String debtId, Debt updatedDebt, @CookieParam("auth_token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }
    
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
            logger.severe("Error updating debt: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

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