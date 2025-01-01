package ss.finance.rest;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;

import ss.finance.Transaction;
import ss.finance.TransactionDTO;
import ss.finance.TransactionZrno;
import ss.finance.security.JwtUtil;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionApi {

    @Inject
    private TransactionZrno transactionZrno;

    @Inject
    private JwtUtil jwtUtil;

    private static final Logger logger = Logger.getLogger(TransactionApi.class.getName());

    @POST
    public Response addTransaction(TransactionDTO transactionDTO, @CookieParam("auth_token") String token) {
        try {
            // Log the input TransactionDTO object
            logger.info("Received transactionDTO: " +
                "type=" + transactionDTO.getType() +
                ", amount=" + transactionDTO.getAmount() +
                ", category=" + transactionDTO.getCategory() +
                ", date=" + transactionDTO.getDate());

            // Validate the date
            if (transactionDTO.getDate() == null) {
                logger.info("Date is missing. Setting current date.");
                transactionDTO.setDate(new Date()); // Default to current date
            } else {
                logger.info("Using provided date: " + transactionDTO.getDate());
            }
    
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }
    
            // Extract the user ID and log it
            ObjectId userId = jwtUtil.extractUserId(token);
            logger.info("Extracted userId: " + userId);
    
            // Create and log the Transaction object
            Transaction transaction = new Transaction(
                    userId,
                    transactionDTO.getType(),
                    transactionDTO.getAmount(),
                    transactionDTO.getCategory(),
                    transactionDTO.getDate() // Pass the date field
            );
            logger.info("Created Transaction object: " +
                "userId=" + transaction.getUserId() +
                ", type=" + transaction.getType() +
                ", amount=" + transaction.getAmount() +
                ", category=" + transaction.getCategory() +
                ", date=" + transaction.getDate());
    
            // Save the transaction
            logger.info("Transaction date before saving: " + transaction.getDate());
            transactionZrno.addTransaction(transaction);
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

            List<TransactionDTO> transactions = transactionZrno.getAllTransactions(userId);

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
}