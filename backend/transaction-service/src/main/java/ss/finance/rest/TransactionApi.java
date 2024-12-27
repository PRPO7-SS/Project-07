package ss.finance.rest;

import ss.finance.TransactionZrno;
import ss.finance.TransactionDTO;
import ss.finance.Transaction;
import ss.finance.security.JwtUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import java.util.List;
import org.bson.types.ObjectId;
import javax.ws.rs.core.Cookie;


@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionApi {

    @Inject
    private TransactionZrno transactionZrno;

    @Inject
    private JwtUtil jwtUtil;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTransaction(TransactionDTO transactionDTO, @Context HttpHeaders headers) {
        // Extract token from Authorization header
        String token = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Authorization header missing or invalid\"}")
                    .build();
        }
        // Remove "Bearer " prefix from token
        token = token.substring(7);

        // Extract userId from the JWT token
        ObjectId userId = jwtUtil.extractUserId(token); // Assuming your JwtUtil class works correctly

        // Create the transaction object using the extracted userId and transaction details
        Transaction transaction = new Transaction(
                userId,
                transactionDTO.getType(),
                transactionDTO.getAmount(),
                transactionDTO.getCategory()
        );

        // Add the transaction to the database or your data source
        transactionZrno.addTransaction(transaction);

        // Return success response
        return Response.status(Response.Status.CREATED)
                .entity("{\"message\": \"Transaction created successfully\"}")
                .build();
    }


    @GET
    public Response getUserTransactions(@Context HttpHeaders headers) {
        //String token = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        Cookie authCookie = headers.getCookies().get("auth_token");
        if (authCookie == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Auth cookie missing\"}")
                    .build();
        }
        String token = authCookie.getValue();
        if (token == null || !token.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\": \"Authorization header missing or invalid\"}")
                    .build();
        }
        // Remove "Bearer " prefix from token
        token = token.substring(7);
        ObjectId userId = jwtUtil.extractUserId(token);

        List<TransactionDTO> transactions = transactionZrno.getAllTransactions(userId);

        return Response.ok(transactions).build();
    }
}
