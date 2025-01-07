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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ss.finance.entities.Transaction;
import ss.finance.security.JwtUtil;
import ss.finance.services.TransactionBean;
import ss.finance.services.TransactionDTO;

@Tag(name = "Transactions", description = "Endpoints for managing user transactions")
@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionApi {

    @Inject
    private TransactionBean transactionBean;

    @Inject
    private JwtUtil jwtUtil;

    private static final Logger logger = Logger.getLogger(TransactionApi.class.getName());

    @Operation(summary = "Add a transaction", description = "Creates a new transaction for the authenticated user")
    @APIResponse(
            responseCode = "201",
            description = "Transaction created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Transaction created successfully\"}"))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid or missing body parameters",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Invalid request body\"}"))
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
    public Response addTransaction(
            @RequestBody(description = "Transaction details", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransactionDTO.class))) 
            TransactionDTO transactionDTO, 
            @CookieParam("auth_token") String token) {
        try {
            logger.info("Received transactionDTO: " + transactionDTO);

            if (transactionDTO.getDate() == null) {
                transactionDTO.setDate(new Date());
            }

            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            Transaction transaction = new Transaction(
                    userId,
                    transactionDTO.getType(),
                    transactionDTO.getAmount(),
                    transactionDTO.getCategory(),
                    transactionDTO.getDate()
            );

            transactionBean.addTransaction(transaction);

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

    @Operation(summary = "Get user transactions", description = "Fetches all transactions for the authenticated user")
    @APIResponse(
            responseCode = "200",
            description = "List of transactions",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "[{\"id\": \"<transaction_id>\", \"type\": \"expense\", \"amount\": 100.0, \"category\": \"Groceries\", \"date\": \"2023-12-01\"}]"))
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
    @GET
    public Response getUserTransactions(@CookieParam("auth_token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
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

    @Operation(summary = "Delete a transaction", description = "Deletes a transaction by ID")
    @APIResponse(
            responseCode = "204",
            description = "Transaction deleted successfully"
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid transaction ID",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Invalid transaction ID format\"}"))
    )
    @APIResponse(
            responseCode = "401",
            description = "Unauthorized access",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Token is missing or invalid\"}"))
    )
    @APIResponse(
            responseCode = "404",
            description = "Transaction not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Transaction not found or does not belong to the user\"}"))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(example = "{\"message\": \"Server error occurred\"}"))
    )
    @DELETE
    @Path("/{transactionId}")
    public Response deleteTransaction(
            @PathParam("transactionId") String transactionId, 
            @CookieParam("auth_token") String token) {
        try {
            if (transactionId == null || transactionId.length() != 24 || !transactionId.matches("[a-fA-F0-9]{24}")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Invalid transaction ID format\"}")
                        .build();
            }

            if (token == null || token.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"message\": \"Token is missing or invalid\"}")
                        .build();
            }

            ObjectId userId = jwtUtil.extractUserId(token);
            ObjectId transactionObjectId = new ObjectId(transactionId);
            Transaction transaction = transactionBean.getTransactionById(transactionObjectId);

            if (transaction == null || !transaction.getUserId().equals(userId)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Transaction not found or does not belong to the user\"}")
                        .build();
            }

            if (transactionBean.deleteTransaction(transactionObjectId)) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"message\": \"Failed to delete transaction\"}")
                        .build();
            }
        } catch (Exception e) {
            logger.severe("Error deleting transaction: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Server error occurred\"}")
                    .build();
        }
    }
}