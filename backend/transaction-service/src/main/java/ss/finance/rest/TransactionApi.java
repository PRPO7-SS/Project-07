package ss.finance.rest;

import java.util.Date;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;

import ss.finance.entities.Transaction;
import ss.finance.security.JwtUtil;
import ss.finance.services.TransactionBean;
import ss.finance.services.TransactionDTO;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionApi {

    @Inject
    private TransactionBean transactionBean;

    @Inject
    private JwtUtil jwtUtil;

    private static final Logger logger = Logger.getLogger(TransactionApi.class.getName());

    @POST
    public Response addTransaction(TransactionDTO transactionDTO, @CookieParam("auth_token") String token) {
        try {
            logger.info("Received transactionDTO: " +
                "type=" + transactionDTO.getType() +
                ", amount=" + transactionDTO.getAmount() +
                ", category=" + transactionDTO.getCategory() +
                ", date=" + transactionDTO.getDate());

            if (transactionDTO.getDate() == null) {
                logger.info("Date is missing. Setting current date.");
                transactionDTO.setDate(new Date()); // Default to current date
            }

            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            logger.info("Extracted userId: " + userId);

            Transaction transaction = new Transaction(
                    userId,
                    transactionDTO.getType(),
                    transactionDTO.getAmount(),
                    transactionDTO.getCategory(),
                    transactionDTO.getDate()
            );

            transactionBean.addTransaction(transaction);
            logger.info("Transaction added successfully: " + transaction);

            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\": \"Transaction created successfully\"}")
                    .build();
        } catch (Exception e) {
            logger.severe("Error adding transaction: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @GET
    public Response getUserTransactions(@CookieParam("auth_token") String token) {
        try {
            logger.info("Received token: " + token);

            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            logger.info("Extracted userId: " + userId);

            var transactions = transactionBean.getTransactionsByUserId(userId);

            if (transactions.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            return Response.ok(transactions).build();
        } catch (Exception e) {
            logger.severe("Error retrieving transactions: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{transactionId}")
    public Response deleteTransaction(@PathParam("transactionId") String transactionId) {
        try {
            // Validate the format of transactionId
            if (transactionId == null || transactionId.length() != 24 || !transactionId.matches("[a-fA-F0-9]{24}")) {
                logger.severe("Invalid transaction ID format: " + transactionId);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Invalid transaction ID format\"}")
                        .build();
            }
    
            logger.info("Received request to delete transaction with ID: " + transactionId);
    
            ObjectId id = new ObjectId(transactionId); // Convert to ObjectId
            if (transactionBean.deleteTransaction(id)) {
                logger.info("Transaction deleted successfully: " + transactionId);
                return Response.status(Response.Status.NO_CONTENT).build(); // 204 No Content
            } else {
                logger.warning("Transaction not found for ID: " + transactionId);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Transaction not found\"}")
                        .build();
            }
        } catch (IllegalArgumentException e) {
            logger.severe("Invalid transaction ID: " + transactionId);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Invalid transaction ID\"}")
                    .build();
        } catch (Exception e) {
            logger.severe("Error deleting transaction: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }
}