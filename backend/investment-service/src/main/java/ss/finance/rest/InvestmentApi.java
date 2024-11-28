package ss.finance.rest;

import ss.finance.services.InvestmentBean;
import ss.finance.entities.Investment;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/investments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InvestmentApi {

    @Inject
    private InvestmentBean investmentBean;

    // Create Investment
    @POST
    public Response addInvestment(Investment investment) {
        try {
            // Add investment to the database
            investmentBean.addInvestment(investment);

            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\": \"Investment created successfully\"}")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Error creating investment\"}")
                    .build();
        }
    }

    // Get All Investments
    @GET
    public Response getAllInvestments() {
        try {
            List<Investment> investments = investmentBean.getAllInvestments();
            return Response.ok(investments).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error retrieving investments\"}")
                    .build();
        }
    }

    // Get Investment by ID
    @GET
    @Path("/{investmentId}")
    public Response getInvestmentById(@PathParam("investmentId") String investmentId) {
        try {
            Investment investment = investmentBean.getInvestmentById(investmentId);

            if (investment == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Investment not found\"}")
                        .build();
            }

            return Response.ok(investment).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error retrieving investment\"}")
                    .build();
        }
    }

    // Update Investment
    @PUT
    @Path("/{investmentId}")
    public Response updateInvestment(@PathParam("investmentId") String investmentId, Investment investment) {
        try {
            investment.setId(new org.bson.types.ObjectId(investmentId));
            investmentBean.updateInvestment(investment);

            return Response.ok("{\"message\": \"Investment updated successfully\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Error updating investment\"}")
                    .build();
        }
    }

    // Delete Investment by ID
    @DELETE
    @Path("/{investmentId}")
    public Response deleteInvestment(@PathParam("investmentId") String investmentId) {
        try {
            investmentBean.deleteInvestment(investmentId);

            return Response.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error deleting investment\"}")
                    .build();
        }
    }
}