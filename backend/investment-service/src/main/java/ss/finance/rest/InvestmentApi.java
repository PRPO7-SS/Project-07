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

@Path("/investments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InvestmentApi {

    @Inject
    private InvestmentBean investmentBean;

    @Inject
    private JwtUtil jwtUtil;

    @GET
    public Response getInvestments(@CookieParam("auth-token") String token) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            List<Investment> investments = investmentBean.getAllInvestments(userId);
            return Response.ok(investments).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Invalid token or user not found")).build();
        }
    }

    @POST
    public Response addInvestment(@CookieParam("auth-token") String token, Investment investment) {
        try {
            ObjectId userId = jwtUtil.extractUserId(token);
            investment.setUserId(userId);
            investmentBean.addInvestment(investment);
            return Response.status(Response.Status.CREATED).entity(Map.of("message", "Investment created successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateInvestment(@CookieParam("auth-token") String token, @PathParam("id") String id, Investment updatedInvestment) {
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

    @DELETE
    @Path("/{id}")
    public Response deleteInvestment(@CookieParam("auth-token") String token, @PathParam("id") String id) {
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
